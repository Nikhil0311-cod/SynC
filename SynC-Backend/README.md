# SynC Backend API

A lightweight, high-performance backend REST API built with Go and SQLite, designed to power the SynC campus community dashboard. The application features user authentication, role-based access control, file uploading, dynamic polls, campus announcements, lost & found tracking, and learning material sharing.

---

## **Features**

* **User Management & Auth**: JWT-based authentication with bcrypt password hashing and support for custom profile avatars.
* **Role-Based Access Control**: Standard student access alongside protected administrative endpoints.
* **Announcements System**: Campus-wide notice board for general and categorized updates.
* **Interactive Polls**: Dynamic voting system with single-vote enforcement per user.
* **Lost & Found**: Community item logging with status and location tracking.
* **Learning Materials**: Multipart file upload service for sharing academic resources.
* **Embedded Static Web Server**: Embeds frontend web assets directly into the Go binary using `go:embed`.

---

## **Tech Stack**

* **Language**: Go (1.22+)
* **Database**: SQLite (via `modernc.org/sqlite` pure-Go driver)
* **Authentication**: JWT (`[github.com/golang-jwt/jwt/v5](https://github.com/golang-jwt/jwt/v5)`) & Bcrypt (`golang.org/x/crypto/bcrypt`)
* **Routing**: Native Go `net/http` ServeMux

---

## **Getting Started**

### **Prerequisites**

* Go 1.22 or higher installed on your system.

### **Installation**

1. Clone the repository:
```bash
git clone https://github.com/your-username/sync-backend.git
cd sync-backend

```


2. Install dependencies:
```bash
go mod tidy

```


3. Set up environment variables (Optional but recommended):
```bash
export JWT_SECRET="your-secure-custom-secret"

```


4. Run the application:
```bash
go run main.go

```



The server will automatically initialize the database schema (`sync.db`), seed default accounts, create necessary upload directories, and start listening on `[http://0.0.0.0:8080](http://0.0.0.0:8080)`.

---

## **System Architecture & How It Works**

```
+------------------+         +----------------------+         +-----------------------+
|  Frontend Client | <-----> |   Go ServeMux Router  | <-----> | SQLite Database       |
|  (Embedded / Web)|         |  (Auth & Middleware) |         | (sync.db)             |
+------------------+         +----------------------+         +-----------------------+
                                        |
                                        v
                             +----------------------+
                             | File System Uploads  |
                             | (public/uploads/)    |
                             +----------------------+

```

### **1. Startup Sequence**

When the application starts, it performs the following setup automatically:

* Connects to `sync.db` using SQLite.
* Executes `initSchema()` to build database tables (`users`, `announcements`, `polls`, `lost_found`, `materials`) if they do not exist.
* Executes `seed()` to create an initial admin account (`admin`) and a demo student account (`demo@sync.edu`). Passwords are automatically hashed via bcrypt.
* Creates the `./public/uploads` local file storage directory.

### **2. Middleware & Request Pipeline**

* **CORS Handling (`withCORS`)**: Configures cross-origin access and sets `ngrok-skip-browser-warning` headers to support local tunneling during development.
* **Authentication (`authenticateToken`)**: Intercepts requests, validates JWT tokens sent via the `Authorization: Bearer <token>` header, extracts the user ID claim, and injects it into the request headers (`X-User-ID`).
* **Admin Verification (`requireAdmin`)**: Fetches user record from DB using `X-User-ID` and confirms the user possesses the `admin` role before allowing access to restricted endpoints.

---

## **API Endpoints**

| Category | Method | Endpoint | Auth Required | Description |
| --- | --- | --- | --- | --- |
| **Auth** | `POST` | `/auth/register` | No | Register a new user account |
| **Auth** | `POST` | `/auth/login` | No | Authenticate user and return JWT |
| **Profile** | `GET` | `/users/me` | Yes | Get active user profile |
| **Profile** | `PUT` | `/users/me` | Yes | Update profile details |
| **Profile** | `POST` | `/users/me/avatar` | Yes | Upload user avatar image |
| **Admin** | `GET` | `/admin/users` | Admin | List all registered users |
| **Admin** | `DELETE` | `/admin/users/{id}` | Admin | Remove a user account |
| **Announcements** | `GET` | `/announcements` | No | Fetch all campus announcements |
| **Announcements** | `POST` | `/announcements` | Yes | Post a new announcement |
| **Announcements** | `DELETE` | `/announcements/{id}` | Yes | Delete an announcement |
| **Polls** | `GET` | `/polls` | Optional | Fetch polls and active user vote status |
| **Polls** | `POST` | `/polls` | Yes | Create a new community poll |
| **Polls** | `POST` | `/polls/{id}/vote` | Yes | Cast a vote on a poll option |
| **Lost & Found** | `GET` | `/lost-found` | No | List reported lost/found items |
| **Lost & Found** | `POST` | `/lost-found` | Yes | Report a new item |
| **Materials** | `GET` | `/materials` | No | List uploaded study resources |
| **Materials** | `POST` | `/materials` | Yes | Upload a new resource file |

---

## **Default Credentials (Seed Data)**

> **Note**: For security, change these default credentials after running the application in a production environment.

* **Admin Account**: `admin` / `admin`
* **Demo Student Account**: `demo@sync.edu` / `password123`
