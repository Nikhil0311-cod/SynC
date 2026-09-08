#!/usr/bin/env bash
set -e

echo ""
echo "  SynC builder"
echo "  ------------"

cd "$(dirname "$0")"

if ! command -v go >/dev/null 2>&1; then
  echo "  Go was not found on this machine."
  echo "  Download and install it from https://go.dev/dl/, then run this again."
  exit 1
fi

if [ ! -f go.mod ]; then
  echo "  Setting up the module..."
  go mod init sync-app-go
fi

echo "  Fetching the SQLite driver (needs internet, one time only)..."
go get modernc.org/sqlite

echo "  Compiling SynC..."
go build -o SynC .

echo ""
echo "  Done. ./SynC is ready in this folder."
echo "  From now on, just run ./SynC - no Go, no Node, nothing else needed."
echo ""
