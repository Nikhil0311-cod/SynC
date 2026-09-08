package main

import (
	"database/sql"
	"embed"
	"encoding/json"
	"fmt"
	"io"
	"io/fs"
	"log"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
	_ "modernc.org/sqlite"
)

//go:embed public/*
var publicFS embed.FS

var db *sql.DB

func getJWTSecret() []byte {
	secret := os.Getenv("JWT_SECRET")
	if secret == "" {
		// Fallback for local development; set JWT_SECRET env var in production
		secret = "default-dev-secret-change-in-production"
	}
	return []byte(secret)
}

// ---------------------------------------------------------------------------
// Models
// ---------------------------------------------------------------------------

type UserDto struct {
	ID        int64   `json:"id"`
	Name      string  `json:"name"`
	Email     string  `json:"email"`
	AvatarURL *string `json:"avatar_url"`
	Role      string  `json:"role"`
	CreatedAt *string `json:"created_at"`
}

type AuthResponseDto struct {
	Token string  `json:"token"`
	User  UserDto `json:"user"`
}

type RegisterRequest struct {
	Name      string  `json:"name"`
	Email     string  `json:"email"`
	Password  string  `json:"password"`
	AvatarURL *string `json:"avatar_url"`
}

type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type UpdateProfileRequest struct {
	Name      string  `json:"name"`
	AvatarURL *string `json:"avatar_url"`
}

type PollOption struct {
	Text  string `json:"text"`
	Votes int    `json:"votes"`
}

type Announcement struct {
	ID        int64  `json:"id"`
	Title     string `json:"title"`
	Category  string `json:"category"`
	Body      string `json:"body"`
	UserID    int64  `json:"user_id"`
	CreatedAt string `json:"created_at"`
}

type Poll struct {
	ID        int64        `json:"id"`
	Question  string       `json:"question"`
	Options   []PollOption `json:"options"`
	UserID    int64        `json:"user_id"`
	HasVoted  bool         `json:"has_voted"`
	CreatedAt string       `json:"created_at"`
}

type LostFoundItem struct {
	ID          int64  `json:"id"`
	ItemName    string `json:"item_name"`
	Status      string `json:"status"`
	Location    string `json:"location"`
	Description string `json:"description"`
	UserID      int64  `json:"user_id"`
	CreatedAt   string `json:"created_at"`
}

type MaterialDto struct {
	ID          int64   `json:"id"`
	Name        string  `json:"name"`
	Course      string  `json:"course"`
	Description *string `json:"description"`
	FileURL     string  `json:"file_url"`
	UserID      int64   `json:"user_id"`
	CreatedAt   string  `json:"created_at"`
}

// ---------------------------------------------------------------------------
// Main & Setup
// ---------------------------------------------------------------------------

func main() {
	var err error
	db, err = sql.Open("sqlite", "sync.db")
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	initSchema()
	seed()

	os.MkdirAll("./public/uploads", 0755)

	subFS, err := fs.Sub(publicFS, "public")
	if err != nil {
		log.Fatal(err)
	}

	mux := http.NewServeMux()

	// Static Dashboard & Assets
	mux.Handle("GET /static/", http.FileServerFS(subFS))
	mux.HandleFunc("GET /{$}", serveDashboard(subFS))

	// Serve Uploads Statically
	mux.Handle("GET /uploads/", http.StripPrefix("/uploads/", http.FileServer(http.Dir("./public/uploads"))))
	mux.Handle("GET /public/uploads/", http.StripPrefix("/public/uploads/", http.FileServer(http.Dir("./public/uploads"))))

	// Auth Endpoints
	mux.HandleFunc("POST /auth/register", handleRegister)
	mux.HandleFunc("POST /auth/login", handleLogin)

	// User Profile Endpoints
	mux.HandleFunc("GET /users/me", authenticateToken(handleGetProfile))
	mux.HandleFunc("PUT /users/me", authenticateToken(handleUpdateProfile))
	mux.HandleFunc("POST /users/me/avatar", authenticateToken(handleUploadAvatar))

	// Protected Admin Management Endpoints
	mux.HandleFunc("GET /admin/users", authenticateToken(requireAdmin(handleListUsers)))
	mux.HandleFunc("DELETE /admin/users/{id}", authenticateToken(requireAdmin(handleDeleteUser)))

	// Announcements
	mux.HandleFunc("GET /announcements", listAnnouncements)
	mux.HandleFunc("POST /announcements", authenticateToken(createAnnouncement))
	mux.HandleFunc("DELETE /announcements/{id}", authenticateToken(deleteAnnouncement))

	// Polls
	mux.HandleFunc("GET /polls", optionalAuthenticateToken(listPolls))
	mux.HandleFunc("POST /polls", authenticateToken(createPoll))
	mux.HandleFunc("POST /polls/{id}/vote", authenticateToken(votePoll))
	mux.HandleFunc("DELETE /polls/{id}", authenticateToken(deletePoll))

	// Lost & Found
	mux.HandleFunc("GET /lost-found", listLostFound)
	mux.HandleFunc("POST /lost-found", authenticateToken(createLostFound))
	mux.HandleFunc("DELETE /lost-found/{id}", authenticateToken(deleteLostFound))

	// Learning Materials
	mux.HandleFunc("GET /materials", listMaterials)
	mux.HandleFunc("POST /materials", authenticateToken(createMaterial))
	mux.HandleFunc("DELETE /materials/{id}", authenticateToken(deleteMaterial))

	handler := withCORS(mux)

	fmt.Println("\n SynC Server running on http://0.0.0.0:8080")

	log.Fatal(http.ListenAndServe("0.0.0.0:8080", handler))
}

func initSchema() {
	stmts := []string{
		`CREATE TABLE IF NOT EXISTS users (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			name TEXT NOT NULL,
			email TEXT UNIQUE NOT NULL,
			password TEXT NOT NULL,
			avatar_url TEXT DEFAULT '',
			role TEXT DEFAULT 'student',
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP
		);`,
		`CREATE TABLE IF NOT EXISTS announcements (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			title TEXT NOT NULL,
			body TEXT NOT NULL,
			category TEXT DEFAULT 'General',
			user_id INTEGER,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY(user_id) REFERENCES users(id)
		);`,
		`CREATE TABLE IF NOT EXISTS polls (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			question TEXT NOT NULL,
			options TEXT NOT NULL,
			user_id INTEGER,
			voted_user_ids TEXT DEFAULT '[]',
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY(user_id) REFERENCES users(id)
		);`,
		`CREATE TABLE IF NOT EXISTS lost_found (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			item_name TEXT NOT NULL,
			description TEXT,
			status TEXT DEFAULT 'lost',
			location TEXT,
			user_id INTEGER,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY(user_id) REFERENCES users(id)
		);`,
		`CREATE TABLE IF NOT EXISTS materials (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			name TEXT NOT NULL,
			course TEXT NOT NULL,
			description TEXT,
			file_url TEXT NOT NULL,
			user_id INTEGER NOT NULL,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY(user_id) REFERENCES users(id)
		);`,
	}
	for _, s := range stmts {
		if _, err := db.Exec(s); err != nil {
			log.Fatal(err)
		}
	}

	_, _ = db.Exec(`ALTER TABLE polls ADD COLUMN voted_user_ids TEXT DEFAULT '[]';`)
}

func seed() {
	var adminExists int
	db.QueryRow(`SELECT COUNT(*) FROM users WHERE email = ?`, "admin").Scan(&adminExists)
	if adminExists == 0 {
		hashedAdminPass, _ := bcrypt.GenerateFromPassword([]byte("admin"), bcrypt.DefaultCost)
		db.Exec(`INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)`,
			"System Admin", "admin", string(hashedAdminPass), "admin")
	}

	var count int
	db.QueryRow(`SELECT COUNT(*) FROM users WHERE email != 'admin'`).Scan(&count)
	if count == 0 {
		hashedUserPass, _ := bcrypt.GenerateFromPassword([]byte("password123"), bcrypt.DefaultCost)
		db.Exec(`INSERT INTO users (id, name, email, password, role) VALUES (?, ?, ?, ?, ?)`,
			123, "Demo Student", "demo@sync.edu", string(hashedUserPass), "student")
	}
}

// ---------------------------------------------------------------------------
// Auth & Middleware Helpers
// ---------------------------------------------------------------------------

func generateToken(userID int64) (string, error) {
	claims := jwt.MapClaims{
		"sub": userID,
		"exp": time.Now().Add(72 * time.Hour).Unix(),
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(getJWTSecret())
}

func authenticateToken(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		authHeader := r.Header.Get("Authorization")
		if authHeader == "" {
			writeError(w, http.StatusUnauthorized, "Missing Authorization header")
			return
		}

		parts := strings.Split(authHeader, " ")
		if len(parts) != 2 || parts[0] != "Bearer" {
			writeError(w, http.StatusUnauthorized, "Invalid authorization format")
			return
		}

		claims := jwt.MapClaims{}
		token, err := jwt.ParseWithClaims(parts[1], claims, func(t *jwt.Token) (interface{}, error) {
			return getJWTSecret(), nil
		})

		if err != nil || !token.Valid {
			writeError(w, http.StatusUnauthorized, "Invalid or expired token")
			return
		}

		sub, ok := claims["sub"].(float64)
		if !ok {
			writeError(w, http.StatusUnauthorized, "Invalid token subject")
			return
		}

		r.Header.Set("X-User-ID", fmt.Sprintf("%d", int64(sub)))
		next.ServeHTTP(w, r)
	}
}

func optionalAuthenticateToken(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		authHeader := r.Header.Get("Authorization")
		if strings.HasPrefix(authHeader, "Bearer ") {
			tokenStr := strings.TrimPrefix(authHeader, "Bearer ")
			claims := jwt.MapClaims{}
			token, err := jwt.ParseWithClaims(tokenStr, claims, func(t *jwt.Token) (interface{}, error) {
				return getJWTSecret(), nil
			})
			if err == nil && token.Valid {
				if sub, ok := claims["sub"].(float64); ok {
					r.Header.Set("X-User-ID", fmt.Sprintf("%d", int64(sub)))
				}
			}
		}
		next.ServeHTTP(w, r)
	}
}

func requireAdmin(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		userID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)
		user, err := getUserByID(userID)
		if err != nil || user.Role != "admin" {
			writeError(w, http.StatusForbidden, "Admin access required")
			return
		}
		next.ServeHTTP(w, r)
	}
}

func getUserByID(id int64) (*UserDto, error) {
	var u UserDto
	var avatar, createdAt sql.NullString
	err := db.QueryRow(`SELECT id, name, email, avatar_url, role, DATE(created_at) FROM users WHERE id = ?`, id).
		Scan(&u.ID, &u.Name, &u.Email, &avatar, &u.Role, &createdAt)
	if err != nil {
		return nil, err
	}
	if avatar.Valid {
		u.AvatarURL = &avatar.String
	}
	if createdAt.Valid {
		u.CreatedAt = &createdAt.String
	}
	return &u, nil
}

func getBaseURL(r *http.Request) string {
	scheme := "http"
	if r.TLS != nil || r.Header.Get("X-Forwarded-Proto") == "https" {
		scheme = "https"
	}
	return fmt.Sprintf("%s://%s", scheme, r.Host)
}

// ---------------------------------------------------------------------------
// Handlers
// ---------------------------------------------------------------------------

func handleRegister(w http.ResponseWriter, r *http.Request) {
	var req RegisterRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	avatar := ""
	if req.AvatarURL != nil {
		avatar = *req.AvatarURL
	}

	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Failed to process password")
		return
	}

	res, err := db.Exec(`INSERT INTO users (name, email, password, avatar_url) VALUES (?, ?, ?, ?)`, req.Name, req.Email, string(hashedPassword), avatar)
	if err != nil {
		writeError(w, http.StatusBadRequest, "Email already exists")
		return
	}

	id, _ := res.LastInsertId()
	token, _ := generateToken(id)
	user, _ := getUserByID(id)

	writeJSON(w, http.StatusCreated, AuthResponseDto{Token: token, User: *user})
}

func handleLogin(w http.ResponseWriter, r *http.Request) {
	var req LoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	var userID int64
	var storedPassword string
	err := db.QueryRow(`SELECT id, password FROM users WHERE email = ?`, req.Email).Scan(&userID, &storedPassword)
	if err != nil {
		writeError(w, http.StatusUnauthorized, "Invalid email or password")
		return
	}

	if err := bcrypt.CompareHashAndPassword([]byte(storedPassword), []byte(req.Password)); err != nil {
		writeError(w, http.StatusUnauthorized, "Invalid email or password")
		return
	}

	token, _ := generateToken(userID)
	user, _ := getUserByID(userID)

	writeJSON(w, http.StatusOK, AuthResponseDto{Token: token, User: *user})
}

func handleGetProfile(w http.ResponseWriter, r *http.Request) {
	userID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)
	user, err := getUserByID(userID)
	if err != nil {
		writeError(w, http.StatusNotFound, "User not found")
		return
	}
	writeJSON(w, http.StatusOK, user)
}

func handleUpdateProfile(w http.ResponseWriter, r *http.Request) {
	userID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)
	var req UpdateProfileRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	avatar := ""
	if req.AvatarURL != nil {
		avatar = *req.AvatarURL
	}

	_, err := db.Exec(`UPDATE users SET name = ?, avatar_url = ? WHERE id = ?`, req.Name, avatar, userID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Failed to update profile")
		return
	}

	user, _ := getUserByID(userID)
	writeJSON(w, http.StatusOK, user)
}

func handleUploadAvatar(w http.ResponseWriter, r *http.Request) {
	userID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)
	if userID == 0 {
		writeError(w, http.StatusUnauthorized, "Unauthorized")
		return
	}

	if err := r.ParseMultipartForm(10 << 20); err != nil {
		writeError(w, http.StatusBadRequest, "File too large or invalid multipart form")
		return
	}

	file, handler, err := r.FormFile("avatar")
	if err != nil {
		writeError(w, http.StatusBadRequest, "Missing 'avatar' file part")
		return
	}
	defer file.Close()

	ext := filepath.Ext(filepath.Base(handler.Filename))
	filename := fmt.Sprintf("avatar_%d_%d%s", userID, time.Now().UnixNano(), ext)
	dstPath := filepath.Join("public", "uploads", filename)

	dst, err := os.Create(dstPath)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Failed to save file on server")
		return
	}
	defer dst.Close()

	if _, err := io.Copy(dst, file); err != nil {
		writeError(w, http.StatusInternalServerError, "Failed to write file content")
		return
	}

	avatarURL := fmt.Sprintf("%s/uploads/%s", getBaseURL(r), filename)

	_, err = db.Exec(`UPDATE users SET avatar_url = ? WHERE id = ?`, avatarURL, userID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Failed to update user avatar in database")
		return
	}

	user, err := getUserByID(userID)
	if err != nil {
		writeError(w, http.StatusNotFound, "User not found")
		return
	}

	writeJSON(w, http.StatusOK, user)
}

// --- Admin Handlers ---

func handleListUsers(w http.ResponseWriter, r *http.Request) {
	rows, err := db.Query(`SELECT id, name, email, avatar_url, role, DATE(created_at) FROM users ORDER BY id DESC`)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	defer rows.Close()

	users := []UserDto{}
	for rows.Next() {
		var u UserDto
		var avatar, createdAt sql.NullString
		if err := rows.Scan(&u.ID, &u.Name, &u.Email, &avatar, &u.Role, &createdAt); err != nil {
			continue
		}
		if avatar.Valid {
			u.AvatarURL = &avatar.String
		}
		if createdAt.Valid {
			u.CreatedAt = &createdAt.String
		}
		users = append(users, u)
	}
	writeJSON(w, http.StatusOK, users)
}

func handleDeleteUser(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	userID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)

	targetID, _ := strconv.ParseInt(id, 10, 64)
	if targetID == userID {
		writeError(w, http.StatusBadRequest, "Cannot delete your own admin account")
		return
	}

	_, err := db.Exec(`DELETE FROM users WHERE id = ?`, targetID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

// --- Announcements ---

func listAnnouncements(w http.ResponseWriter, r *http.Request) {
	rows, err := db.Query(`SELECT id, title, category, body, COALESCE(user_id, 0), created_at FROM announcements ORDER BY id DESC`)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	defer rows.Close()

	list := []Announcement{}
	for rows.Next() {
		var a Announcement
		rows.Scan(&a.ID, &a.Title, &a.Category, &a.Body, &a.UserID, &a.CreatedAt)
		list = append(list, a)
	}
	writeJSON(w, http.StatusOK, list)
}

func createAnnouncement(w http.ResponseWriter, r *http.Request) {
	userID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)
	var a Announcement
	if err := json.NewDecoder(r.Body).Decode(&a); err != nil {
		writeError(w, http.StatusBadRequest, "Invalid body")
		return
	}
	a.UserID = userID

	res, err := db.Exec(`INSERT INTO announcements (title, category, body, user_id) VALUES (?, ?, ?, ?)`,
		a.Title, a.Category, a.Body, a.UserID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	id, _ := res.LastInsertId()
	db.QueryRow(`SELECT id, title, category, body, user_id, created_at FROM announcements WHERE id = ?`, id).
		Scan(&a.ID, &a.Title, &a.Category, &a.Body, &a.UserID, &a.CreatedAt)

	writeJSON(w, http.StatusCreated, a)
}

func deleteAnnouncement(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	db.Exec(`DELETE FROM announcements WHERE id = ?`, id)
	w.WriteHeader(http.StatusNoContent)
}

// --- Polls ---

func listPolls(w http.ResponseWriter, r *http.Request) {
	currentUserID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)

	rows, err := db.Query(`SELECT id, question, options, COALESCE(user_id, 0), COALESCE(voted_user_ids, '[]'), created_at FROM polls ORDER BY id DESC`)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	defer rows.Close()

	list := []Poll{}
	for rows.Next() {
		var p Poll
		var optsJSON, votedJSON string
		rows.Scan(&p.ID, &p.Question, &optsJSON, &p.UserID, &votedJSON, &p.CreatedAt)
		json.Unmarshal([]byte(optsJSON), &p.Options)

		var votedIDs []int64
		json.Unmarshal([]byte(votedJSON), &votedIDs)
		p.HasVoted = false
		for _, vid := range votedIDs {
			if vid == currentUserID && currentUserID != 0 {
				p.HasVoted = true
				break
			}
		}

		list = append(list, p)
	}
	writeJSON(w, http.StatusOK, list)
}

func createPoll(w http.ResponseWriter, r *http.Request) {
	userID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)

	var input struct {
		Question string   `json:"question"`
		Options  []string `json:"options"`
	}
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "Invalid payload")
		return
	}

	opts := make([]PollOption, len(input.Options))
	for i, txt := range input.Options {
		opts[i] = PollOption{Text: txt, Votes: 0}
	}
	optsBytes, _ := json.Marshal(opts)

	res, err := db.Exec(`INSERT INTO polls (question, options, user_id, voted_user_ids) VALUES (?, ?, ?, '[]')`,
		input.Question, string(optsBytes), userID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	id, _ := res.LastInsertId()
	var p Poll
	var optsJSON string
	db.QueryRow(`SELECT id, question, options, user_id, created_at FROM polls WHERE id = ?`, id).
		Scan(&p.ID, &p.Question, &optsJSON, &p.UserID, &p.CreatedAt)
	json.Unmarshal([]byte(optsJSON), &p.Options)
	p.HasVoted = false

	writeJSON(w, http.StatusCreated, p)
}

func votePoll(w http.ResponseWriter, r *http.Request) {
	userID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)
	id := r.PathValue("id")

	var req struct {
		OptionIndex int `json:"optionIndex"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "Invalid body")
		return
	}

	_, err := getUserByID(userID)
	if err != nil {
		writeError(w, http.StatusUnauthorized, "User not found")
		return
	}

	var p Poll
	var optsJSON, votedJSON string
	err = db.QueryRow(`SELECT id, question, options, user_id, COALESCE(voted_user_ids, '[]'), created_at FROM polls WHERE id = ?`, id).
		Scan(&p.ID, &p.Question, &optsJSON, &p.UserID, &votedJSON, &p.CreatedAt)
	if err != nil {
		writeError(w, http.StatusNotFound, "Poll not found")
		return
	}
	json.Unmarshal([]byte(optsJSON), &p.Options)

	var votedIDs []int64
	json.Unmarshal([]byte(votedJSON), &votedIDs)

	for _, vid := range votedIDs {
		if vid == userID {
			writeError(w, http.StatusBadRequest, "User has already voted on this poll")
			return
		}
	}

	if req.OptionIndex < 0 || req.OptionIndex >= len(p.Options) {
		writeError(w, http.StatusBadRequest, "Invalid option index")
		return
	}
	p.Options[req.OptionIndex].Votes++
	votedIDs = append(votedIDs, userID)

	updatedOpts, _ := json.Marshal(p.Options)
	updatedVoted, _ := json.Marshal(votedIDs)

	db.Exec(`UPDATE polls SET options = ?, voted_user_ids = ? WHERE id = ?`, string(updatedOpts), string(updatedVoted), id)

	p.HasVoted = true
	writeJSON(w, http.StatusOK, p)
}

func deletePoll(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	db.Exec(`DELETE FROM polls WHERE id = ?`, id)
	w.WriteHeader(http.StatusNoContent)
}

// --- Lost & Found ---

func listLostFound(w http.ResponseWriter, r *http.Request) {
	rows, err := db.Query(`SELECT id, item_name, status, location, description, COALESCE(user_id, 0), created_at FROM lost_found ORDER BY id DESC`)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	defer rows.Close()

	list := []LostFoundItem{}
	for rows.Next() {
		var item LostFoundItem
		rows.Scan(&item.ID, &item.ItemName, &item.Status, &item.Location, &item.Description, &item.UserID, &item.CreatedAt)
		list = append(list, item)
	}
	writeJSON(w, http.StatusOK, list)
}

func createLostFound(w http.ResponseWriter, r *http.Request) {
	userID, _ := strconv.ParseInt(r.Header.Get("X-User-ID"), 10, 64)
	var item LostFoundItem
	if err := json.NewDecoder(r.Body).Decode(&item); err != nil {
		writeError(w, http.StatusBadRequest, "Invalid payload")
		return
	}
	item.UserID = userID

	res, err := db.Exec(`INSERT INTO lost_found (item_name, status, location, description, user_id) VALUES (?, ?, ?, ?, ?)`,
		item.ItemName, item.Status, item.Location, item.Description, item.UserID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	id, _ := res.LastInsertId()
	db.QueryRow(`SELECT id, item_name, status, location, description, user_id, created_at FROM lost_found WHERE id = ?`, id).
		Scan(&item.ID, &item.ItemName, &item.Status, &item.Location, &item.Description, &item.UserID, &item.CreatedAt)

	writeJSON(w, http.StatusCreated, item)
}

func deleteLostFound(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	db.Exec(`DELETE FROM lost_found WHERE id = ?`, id)
	w.WriteHeader(http.StatusNoContent)
}

// --- Learning Materials ---

func listMaterials(w http.ResponseWriter, r *http.Request) {
	rows, err := db.Query(`SELECT id, name, course, description, file_url, user_id, created_at FROM materials ORDER BY id DESC`)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	defer rows.Close()

	list := []MaterialDto{}
	for rows.Next() {
		var m MaterialDto
		var desc sql.NullString
		if err := rows.Scan(&m.ID, &m.Name, &m.Course, &desc, &m.FileURL, &m.UserID, &m.CreatedAt); err != nil {
			continue
		}
		if desc.Valid {
			m.Description = &desc.String
		}
		list = append(list, m)
	}
	writeJSON(w, http.StatusOK, list)
}

func createMaterial(w http.ResponseWriter, r *http.Request) {
	if err := r.ParseMultipartForm(50 << 20); err != nil {
		writeError(w, http.StatusBadRequest, "File too large or invalid multipart form")
		return
	}

	name := r.FormValue("name")
	course := r.FormValue("course")
	description := r.FormValue("description")

	userIDStr := r.Header.Get("X-User-ID")
	userID, _ := strconv.ParseInt(userIDStr, 10, 64)

	if name == "" || course == "" || userID == 0 {
		writeError(w, http.StatusBadRequest, "Missing required fields: name, course, or user_id")
		return
	}

	file, handler, err := r.FormFile("file")
	if err != nil {
		writeError(w, http.StatusBadRequest, "Missing 'file' attachment part")
		return
	}
	defer file.Close()

	ext := filepath.Ext(filepath.Base(handler.Filename))
	filename := fmt.Sprintf("material_%d_%d%s", userID, time.Now().UnixNano(), ext)
	dstPath := filepath.Join("public", "uploads", filename)

	dst, err := os.Create(dstPath)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Failed to save file on server")
		return
	}
	defer dst.Close()

	if _, err := io.Copy(dst, file); err != nil {
		writeError(w, http.StatusInternalServerError, "Failed to write file content")
		return
	}

	fileURL := fmt.Sprintf("%s/uploads/%s", getBaseURL(r), filename)

	res, err := db.Exec(`INSERT INTO materials (name, course, description, file_url, user_id) VALUES (?, ?, ?, ?, ?)`,
		name, course, description, fileURL, userID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	id, _ := res.LastInsertId()
	var m MaterialDto
	var desc sql.NullString
	db.QueryRow(`SELECT id, name, course, description, file_url, user_id, created_at FROM materials WHERE id = ?`, id).
		Scan(&m.ID, &m.Name, &m.Course, &desc, &m.FileURL, &m.UserID, &m.CreatedAt)
	if desc.Valid {
		m.Description = &desc.String
	}

	writeJSON(w, http.StatusCreated, m)
}

func deleteMaterial(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")

	var fileURL string
	err := db.QueryRow(`SELECT file_url FROM materials WHERE id = ?`, id).Scan(&fileURL)
	if err == nil && fileURL != "" {
		parts := strings.Split(fileURL, "/uploads/")
		if len(parts) == 2 {
			_ = os.Remove(filepath.Join("public", "uploads", parts[1]))
		}
	}

	_, err = db.Exec(`DELETE FROM materials WHERE id = ?`, id)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

func serveDashboard(subFS fs.FS) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		data, err := fs.ReadFile(subFS, "index.html")
		if err != nil {
			http.Error(w, "Dashboard file not found", http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "text/html; charset=utf-8")
		w.Write(data)
	}
}

func withCORS(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("ngrok-skip-browser-warning", "true")
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization, ngrok-skip-browser-warning")

		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}
		next.ServeHTTP(w, r)
	})
}

func writeJSON(w http.ResponseWriter, status int, v interface{}) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(v)
}

func writeError(w http.ResponseWriter, status int, msg string) {
	writeJSON(w, status, map[string]string{"error": msg})
}

func openBrowser(url string) {
	var cmd *exec.Cmd
	switch runtime.GOOS {
	case "windows":
		cmd = exec.Command("rundll32", "url.dll,FileProtocolHandler", url)
	case "darwin":
		cmd = exec.Command("open", url)
	default:
		cmd = exec.Command("xdg-open", url)
	}
	_ = cmd.Start()
}