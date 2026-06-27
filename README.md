# SWEN 2 — Tour Planner

University semester project for SWEN 2 (FH Technikum Wien, Summer 2026).
Web app for planning bike / hike / running / vacation tours and logging
completed runs of them.

**Authors:** Abris Mezo, Adrian Strassnig
**Repository:** https://github.com/<your-org>/SWEN_2_Tour_Planner

---

## Stack

- **Backend:** Java 21 + Spring Boot 4 (REST + JPA/Hibernate + Spring Security with JWT)
- **Frontend:** Angular 21 (standalone components, modern control flow)
- **Database:** PostgreSQL 17
- **Maps:** Leaflet + the OpenRouteService Directions API
- **Logging:** Log4j 2
- **Tests:** JUnit 5 + Mockito + AssertJ (72 tests at last count)

## Repository layout

```
backend/              Spring Boot app (Maven, ./mvnw)
ui/                   Angular app (npm)
db/                   Postgres init.sql
documentation/        Spec PDF, protocols, checklists, UML
docker-compose.yml    Brings up Postgres + backend together
.env.example          Template for the env vars you need to set
```

## Running it

### Prerequisites
- JDK 21
- Node 20+ / npm
- Docker (only if you want the one-shot docker-compose path)
- A free OpenRouteService API key — https://openrouteservice.org/

### 1. Set up env vars
```bash
cp .env.example .env
# then edit .env and fill in JWT_SECRET and OPENROUTESERVICE_APIKEY
```

### 2. Backend
The easy path — docker-compose brings up Postgres + backend in one shot
and reads `.env` automatically:
```bash
docker-compose up --build
```

Backend listens on `http://localhost:8080`.

### 3. Frontend
```bash
cd ui
npm install
npm start
```

Frontend listens on `http://localhost:4200` and talks to the backend on `:8080`.

## Running the tests

```bash
cd backend
./mvnw test
```

## Spec / docs

- `documentation/semester-project.pdf` — the original assignment
- `documentation/TourPlanner_Checklist_Final.xlsx` — grading checklist
- `documentation/SWEN2_Intermediate_Protocol.pdf` — intermediate submission
- `documentation/` — UML diagrams, wireframes, final protocol (forthcoming)
