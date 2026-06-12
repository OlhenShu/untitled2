# GitHub Repo & Invite Manager

A web application for:
1. **Bulk-creating GitHub repositories** from a template in an organisation.
2. **Bulk-inviting collaborators** (read from a file) to those repositories.

**Stack:** Java 17 + Spring Boot 3 (backend) · React + Vite + TypeScript + Tailwind (frontend)

---

## Prerequisites

- Java 17+, Maven 3.9+ (or Docker + Docker Compose)
- Node 20+ (for local frontend dev)
- A GitHub **Fine-Grained Personal Access Token** (see below)

---

## Creating a GitHub Fine-Grained Token

1. Go to **GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens**
2. Click **Generate new token**
3. Set **Resource owner** to the **organisation** you want to manage (important — if you pick "Only select repositories", the token won't be able to create new ones)
4. Set an expiration date
5. Under **Repository permissions** set:
   - **Administration → Read and write** — required to create repos and manage collaborators
   - **Metadata → Read-only** — enabled by default, do not remove
6. Click **Generate token** and copy it immediately

> The token is sent directly from your browser to GitHub via the `Authorization: Bearer …` header.  
> It is **never** stored in localStorage, cookies, or any database — only in the browser's in-memory React state for the duration of your session.

---

## Running locally (without Docker)

### Backend

```bash
cd backend

# Option A — with token as env variable (recommended)
GITHUB_TOKEN=github_pat_YOUR_TOKEN ./mvnw spring-boot:run

# Option B — without env variable (enter token in the UI)
./mvnw spring-boot:run
```

The backend starts on **http://localhost:8080**.

### Frontend

```bash
cd frontend
cp .env.example .env        # sets VITE_API_URL=http://localhost:8080
npm install
npm run dev
```

Open **http://localhost:5173** in your browser.

---

## Running with Docker Compose

```bash
# Set your token in the environment (optional — can also be entered in UI)
export GITHUB_TOKEN=github_pat_YOUR_TOKEN

docker compose up --build
```

- Frontend: **http://localhost:3000**
- Backend API: **http://localhost:8080/api**

In Docker mode the frontend nginx proxies `/api/*` to the backend, so no CORS setup is needed.

---

## Running tests

```bash
cd backend
./mvnw test
```

---

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/health` | Health check |
| `POST` | `/api/repos/generate` | Bulk-create repos from template |
| `POST` | `/api/invites/send` | Bulk-send collaborator invitations |

Pass the token via `X-GitHub-Token` request header (takes priority over the `GITHUB_TOKEN` env variable).

### `POST /api/repos/generate` — example body

```json
{
  "templateOwner": "my-org",
  "templateRepo": "lab-template",
  "targetOrg": "my-org",
  "namingMode": "PATTERN",
  "baseName": "lab",
  "count": 30,
  "startIndex": 1,
  "padding": 2,
  "visibility": "private",
  "includeAllBranches": false,
  "description": "Student lab: {name}"
}
```

Or with a custom list:

```json
{
  "templateOwner": "my-org",
  "templateRepo": "lab-template",
  "targetOrg": "my-org",
  "namingMode": "LIST",
  "repoNames": ["repo-alice", "repo-bob", "repo-charlie"],
  "visibility": "private",
  "includeAllBranches": false
}
```

### `POST /api/invites/send` — example body

```json
{
  "rawUsernames": "octocat\n@hubot\ndefunkt",
  "repositories": ["my-org/lab-01", "my-org/lab-02"],
  "permission": "push"
}
```

---

## Input file formats

**usernames.txt** (for Invite tab):
```
# comment lines are ignored
octocat
@hubot
  defunkt
```
→ parsed as `["octocat", "hubot", "defunkt"]`

**repo-names.txt** (for Create tab, List mode):
```
repo-alice
repo-bob
repo-charlie
# this line is ignored
```

---

## Notes & limitations

- **Rate limits:** the backend processes requests sequentially with a small delay (150–200 ms) between calls and retries up to 3 times with exponential back-off on 429/403/5xx responses.
- **`internal` visibility:** GitHub's template-generation endpoint only accepts `private: boolean`. If you select *Internal*, the repository is created as public. Set internal visibility manually afterward via the GitHub UI / API, or use a post-creation PATCH call.
- The template repository must be marked as a **Template repository** in its settings.
- The token owner must be a **member** of `targetOrg` with sufficient permissions.
