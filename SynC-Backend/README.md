# SynC (Go edition) — one file, no runtime dependencies

This is the same SynC backend as the Node.js version, rewritten in Go so it compiles
into a single standalone executable. No Node, no npm, no `node_modules` — once built,
`SynC.exe` needs nothing else installed on the machine that runs it.

## Why Go instead of Node here

Go compiles your whole program — the web server, the SQLite driver, and the
dashboard HTML — into one native binary. There's no separate runtime to ship or
install, unlike Node.js apps which always need a Node install (or a bundling tool
with its own trade-offs) to run.

## Build it (one time, needs internet)

1. Install Go from **https://go.dev/dl/** (the regular installer, same as installing anything else — you only do this once, on your own dev machine, not on machines you hand the .exe to).
2. Double-click **`build.bat`** (Windows) or run **`./build.sh`** (Mac/Linux).

This fetches the SQLite driver and compiles `SynC.exe` (or `SynC` on Mac/Linux) right there in the folder.

## Run it

Double-click `SynC.exe`. It will:
- create `sync.db` next to itself, seeded with sample data, on first run
- start listening on **http://localhost:8080**
- open your browser to the dashboard automatically

That `.exe` is now fully portable — copy it (and it alone) to another Windows machine and it'll run there too, no install step required on that machine.

## API — identical to the Node version

| Method | Path                    | Body                                        |
|--------|--------------------------|----------------------------------------------|
| POST   | `/auth/login`             | `{ email, password }` — demo: `demo@sync.edu` / `password123` |
| GET    | `/announcements`          | —                                             |
| POST   | `/announcements`          | `{ title, body, category? }`                  |
| DELETE | `/announcements/:id`      | —                                             |
| GET    | `/polls`                  | —                                             |
| POST   | `/polls`                  | `{ question, options: string[] }`             |
| POST   | `/polls/:id/vote`         | `{ optionIndex }`                             |
| DELETE | `/polls/:id`              | —                                             |
| GET    | `/lost-found`             | —                                             |
| POST   | `/lost-found`             | `{ item_name, description?, status?, location? }` |
| DELETE | `/lost-found/:id`         | —                                             |

From an Android emulator, use `http://10.0.2.2:8080` instead of `localhost:8080` to reach a server running on your host machine.

## What to know before this handles real data

- The mock JWT is unsigned, same caveat as the Node version — swap in real signing and hashed passwords before real student data touches this.
- CORS is wide open (`Access-Control-Allow-Origin: *`) for easy local testing; restrict it before deploying anywhere public.

## A note on testing

This code hasn't been compiled or run in the environment that wrote it (no Go
toolchain or network access there) — it's written carefully against Go's standard
library and the well-documented `modernc.org/sqlite` driver, but if `go build` turns
up an error, paste it back and it'll get fixed.
