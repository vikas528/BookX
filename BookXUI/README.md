# BookXUI — React Front-End

React 19 + TypeScript single-page application for the BookX seat booking platform. Built with Vite, Framer Motion, and Axios. In Docker it is served via nginx which also proxies all API calls to the Gateway.

- **Dev server port:** `3000` (Vite proxy → Gateway `:8443`)
- **Docker port:** `5173` → nginx `:80`

---

## Table of Contents

- [Features](#features)
- [Quick Start — Dev Server](#quick-start--dev-server)
- [Quick Start — Docker](#quick-start--docker)
- [API Integration](#api-integration)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Scripts](#scripts)
- [Tech Stack](#tech-stack)

---

## Features

| Feature | Details |
|---------|---------|
| **Show Catalogue** | Browsable show grid with animated cards |
| **Real-time Seat Map** | Fetches live seat availability; colour-coded by status (Available / Locked / Booked) |
| **Booking Flow** | Select seat -> confirm -> instant booking reference |
| **Booking Lookup** | Search by booking reference `BXS-...` |
| **Animations** | Page transitions, staggered seat rendering, confetti on success (Framer Motion) |
| **Loaders** | Skeleton cards, skeleton seats, spinner overlays, pulsing text |
| **Toast Notifications** | Slide-in toasts for errors and confirmations |
| **Responsive** | Mobile-first layout |

---

## Quick Start — Dev Server

```bash
cd BookXUI

# Install dependencies
npm install

# Start dev server (port 3000)
# Vite proxies /bookxshow/* and /api/* to localhost:8080 automatically
npm run dev
```

The Vite dev server proxies `/bookxshow/*` to `http://localhost:8080` so CORS is handled transparently during development. Make sure the backend services are running first.

For mock mode (no backend required):

```bash
npm run dev:mock
```

---

## Quick Start — Docker

The UI Dockerfile performs a production build and serves it with nginx. nginx also reverse-proxies all backend paths (`/api/`, `/bookxshow/`, `/oauth2/`, `/login`) to the Gateway container on the internal Docker network.

```bash
# From the BookX/ root
docker compose up -d --build ui

# Or build the image standalone
docker build -t bookx-ui:latest ./BookXUI
docker run -d -p 5173:80 bookx-ui:latest
```

Access at **http://localhost:5173**.

### nginx Proxy Rules (Docker)

| Path | Proxied to |
|------|------------|
| `/api/**` | `http://gateway:8443/api/` |
| `/bookxshow/**` | `http://gateway:8443/bookxshow/` |
| `/oauth2/**` | `http://gateway:8443/oauth2/` |
| `/login` | `http://gateway:8443/login` |
| everything else | `/index.html` (SPA routing) |

All other paths are served as static files from the Vite build output.

---

## API Integration

The UI calls these backend endpoints via relative paths (proxied by Vite in dev, by nginx in Docker):

| Method | Path | Page |
|--------|------|------|
| `POST` | `/api/auth/login` | Login |
| `POST` | `/api/auth/signup` | Signup |
| `GET` | `/bookxshow/v1/shows/{showId}/seats` | Seat Selection |
| `POST` | `/bookxshow/v1/bookings` | Booking Flow |
| `GET` | `/bookxshow/v1/bookings/{ref}` | Booking Lookup |
| `DELETE` | `/bookxshow/v1/bookings/{ref}` | Cancel Booking |
| `GET` | `/bookxshow/v1/users/{userId}/bookings` | Booking History |

API client code lives in `src/api/` — one file per resource with a shared Axios instance and request/response interceptors.

---

## Project Structure

```
src/
├── api/              # Axios client + per-resource API modules + config
├── assets/           # Static images, icons
├── components/
│   ├── booking/      # BookingForm
│   ├── common/       # Navbar, Footer, Loader, Skeletons, Toast, Confetti, ErrorDisplay
│   ├── seats/        # SeatMap (interactive grid + legend)
│   └── shows/        # ShowCard, ShowList
├── data/             # Static show catalogue (src/data/shows.ts)
├── hooks/            # useApi — generic data-fetching hook
├── mocks/            # axios-mock-adapter setup for dev:mock mode
├── pages/            # HomePage, SeatPage, ConfirmationPage, LookupPage
├── types/            # TypeScript interfaces matching backend DTOs
├── App.tsx           # Router + AnimatePresence wrapper
├── main.tsx          # React entry point
└── index.css         # Global styles, CSS custom properties, component styles
```

**Note on show catalogue:** `src/data/shows.ts` contains a static list of shows. Each entry's `id` must match the `externalShowId` in your database after syncing from the Admin Service.

---

## Configuration

### Development

Edit `vite.config.ts` to change the proxy target:

```typescript
server: {
  port: 3000,
  proxy: {
    '/bookxshow': { target: 'http://localhost:8080', changeOrigin: true },
    '/api':        { target: 'http://localhost:8443', changeOrigin: true },
  },
},
```

### Docker

The nginx proxy target is hardcoded to `http://gateway:8443` in `nginx.conf`. This works because all containers communicate on the same Docker network.

---

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start Vite dev server on port 3000 (real backend) |
| `npm run dev:mock` | Start Vite dev server with mock API (no backend needed) |
| `npm run build` | TypeScript check + production build to `dist/` |
| `npm run preview` | Preview the production build locally |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | React 19 + TypeScript |
| Build tool | Vite 8 |
| Routing | React Router v7 |
| Animations | Framer Motion 12 |
| HTTP client | Axios + axios-mock-adapter (dev mocks) |
| Styling | CSS Custom Properties — no CSS framework |
| Container | nginx 1.27-alpine (Docker) |

---

## Dockerfile Highlights

| Feature | Detail |
|---------|--------|
| Build image | `node:20-alpine` |
| Runtime image | `nginx:1.27-alpine` (serves static assets) |
| Build command | `npm ci && npm run build` |
| SPA support | nginx `try_files` falls back to `index.html` |
| API proxy | All backend paths forwarded to `gateway:8443` |
| Asset caching | 1-year cache + `immutable` for hashed bundles |