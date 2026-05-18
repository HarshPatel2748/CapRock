# CaptionAI — Build Workflow & Decisions

> Paste this file (along with the Master Brief) at the start of any new chat.
> It tells the AI what's already been decided, what order we're building in,
> and which step we're currently on.

---

## 1. LOCKED DECISIONS

These are settled. Do not re-propose alternatives unless explicitly asked.

| Decision | Choice | Why |
|---|---|---|
| Database hosting | **Neon** (serverless Postgres) | Brief recommends it. Free tier handles CaptionAI workload. Keeps Railway's $5/month dedicated to compute. |
| Schema management | **JPA `ddl-auto=update`** in dev | Fastest iteration. Switch to `validate` before going live so accidental entity changes can't mutate prod schema. |
| Gemini model | **`gemini-3-flash-preview`** | Brief's primary choice. Do NOT set temperature — leave it at default 1.0. |
| Backend package root | **`com.captionai`** | Per brief. |
| Service pattern | **Interface + `service/impl/`** | Matches my ecom reference project. Cleaner for testing. |
| Java / Spring Boot | **Java 17, Spring Boot 3.x** | Modern baseline. |
| Lombok | **Yes** | Reduce entity boilerplate. |
| Backend hosting | **Railway** ($5/month paid plan, no sleep) | Per brief. |
| Frontend hosting | **Vercel** | Per brief. |
| Frontend stack | **React + Vite + Tailwind + MUI + Headless UI + React Icons** | Per brief Section 3. Dark mode only. |
| HTTP client | **Axios** with JWT interceptor | Per brief. |
| Auth | **Spring Security + JWT**, 24h expiry, `Bearer` header, frontend stores in `localStorage` as `cai_token` | Per brief Section 8. |
| Admin | **Hardcoded** via env vars `ADMIN_EMAIL` + `ADMIN_PASSWORD`. No admin table. JWT carries `role: ADMIN` or `role: USER`. | Per brief Section 6. |
| Image handling | **Base64 in browser → POST body → Gemini → discard.** Never persisted anywhere. | Per brief Section 9. Core product promise. |
| Endpoint testing | **Postman** at every backend step | Test before moving on. |

---

## 2. BACKEND PACKAGE STRUCTURE (locked)

```
com.captionai/
├── CaptionAiApplication.java
├── config/          (SecurityConfig, CorsConfig, RazorpayConfig, JwtConfig)
├── controller/      (Auth, Caption, Brand, Saved, Payment, Admin)
├── dto/             (request + response DTOs)
├── exception/       (GlobalExceptionHandler + custom exceptions)
├── model/           (User, BrandProfile, SavedCaption, Payment — JPA entities)
├── repository/      (interfaces extending JpaRepository)
├── security/
│   ├── jwt/         (JwtUtil, JwtFilter, JwtAuthEntryPoint)
│   ├── request/     (LoginRequest, SignupRequest)
│   ├── response/    (UserInfoResponse, MessageResponse)
│   └── services/    (UserDetailsServiceImpl, UserDetailsImpl)
└── service/
    ├── AuthService.java          (interface)
    ├── GeminiService.java        (interface)
    ├── CreditService.java        (interface)
    ├── RazorpayService.java      (interface)
    ├── AdminService.java         (interface)
    └── impl/
        ├── AuthServiceImpl.java
        ├── GeminiServiceImpl.java
        ├── CreditServiceImpl.java
        ├── RazorpayServiceImpl.java
        └── AdminServiceImpl.java
```

---

## 3. FULL BUILD ORDER

### Phase 1 — Backend (local dev against Neon)

- **Step 1: Scaffold** — `pom.xml` (web, security, jpa, postgres driver, jjwt, lombok, validation, razorpay-java, dotenv-java for local dev), package skeleton, `application.properties` reading from env vars, all four JPA entities (`User`, `BrandProfile`, `SavedCaption`, `Payment`) with relationships + indexes, all four repositories. **Smoke test:** app boots, Hibernate creates tables in Neon.
- **Step 2: Security + Auth** — `SecurityConfig`, JWT (`JwtUtil`, `JwtFilter`, `JwtAuthEntryPoint`), `UserDetailsServiceImpl`, `AuthController` with `/register`, `/login`, `/me`. Hardcoded admin check inside login (env vars, no DB lookup, JWT with `role: ADMIN`). **Postman test:** all three endpoints, both USER and ADMIN paths.
- **Step 3: Brand profiles CRUD** — `BrandController` + service + plan-limit enforcement (Free: 0, Starter: 3, Pro: unlimited). **Postman test.**
- **Step 4: Saved captions CRUD** — `SavedController` + service. **Postman test.**
- **Step 5: Caption generation** — `CaptionController` + `GeminiService` calling `gemini-3-flash-preview`. Credit deduction server-side, HTTP 402 if zero. **Postman test** with a real base64 image.
- **Step 6: Payment endpoints (no webhook yet)** — `PaymentController` with `create-order` and `verify`. `RazorpayService` builds orders and verifies HMAC signatures. **Postman test** `create-order` only.
- **Step 7: Admin endpoints** — `AdminController` for stats, user list, manual credit/plan adjust. **Postman test** with admin JWT.
- **Step 8: Exception handling polish** — `GlobalExceptionHandler` covering validation errors, 401, 402, 403, 404, 500.

### Phase 2 — Deploy backend

- **Step 9: Push to GitHub** — `.gitignore` excludes `target/` and `.env`. README with run instructions.
- **Step 10: Deploy to Railway** — connect repo, paste env vars (Neon `DATABASE_URL`, `JWT_SECRET`, `GEMINI_API_KEY`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET`, `RESEND_API_KEY`, `FRONTEND_URL` placeholder, `ADMIN_EMAIL`, `ADMIN_PASSWORD`). Switch `ddl-auto` to `validate` for prod. **Postman test** all endpoints against the Railway URL.

### Phase 3 — Frontend

- **Step 11: Scaffold** — Vite + React, Tailwind config, MUI + Headless UI + React Icons installed, folder structure per brief, Axios instance with `baseURL` from `VITE_API_URL` + JWT interceptor, `AuthContext` + `useAuth` hook.
- **Step 12: Landing page** — Navbar, Hero, Trust bar, How it works, Privacy section, Pricing, Footer. Dark mode, premium SaaS aesthetic.
- **Step 13: AuthModal** — sign in + sign up, stores JWT in `localStorage` as `cai_token`.
- **Step 14: Dashboard shell** — `Layout.jsx` with Sidebar (logo, nav, user card, `CreditMeter`, upgrade prompt when credits ≤ 5).
- **Step 15: NewPost workspace** — `UploadZone` (drag-and-drop, base64 in browser only, privacy note visible), platform dropdown, brand profile dropdown, key detail textarea, style note input, credit warning banner, Generate button. `CaptionCard` output for Professional / Witty / Minimalist with Save + Copy buttons.
- **Step 16: Brands page** — CRUD UI for brand profiles, plan limits enforced visually.
- **Step 17: Saved page** — list of saved captions, delete action.
- **Step 18: Settings page** — account info.
- **Step 19: Billing page** — pricing cards. Buy button wired in next phase.
- **Step 20: Admin dashboard** — `/admin` route protected by `role === 'ADMIN'`. Stats, user list, manual credit/plan adjustment.

### Phase 4 — Razorpay end-to-end

- **Step 21: Frontend Razorpay integration** — load Checkout SDK, wire Billing page button: `create-order` → open Razorpay modal → on success `verify` → on backend success refresh user. **Test with a real Razorpay test transaction.**
- **Step 22: Deploy frontend to Vercel** — push to GitHub, import to Vercel, env vars `VITE_API_URL` and `VITE_RAZORPAY_KEY_ID`. Update Railway's `FRONTEND_URL` to the Vercel domain. Adjust `CorsConfig` if needed.
- **Step 23: Razorpay webhook** — once a real test transaction has succeeded end-to-end, register webhook URL `https://<railway-domain>/api/payment/webhook` in the Razorpay dashboard. Handle monthly renewal events: reset credits for the matching user.

### Phase 5 — Polish & go live

- **Step 24: Resend integration** — welcome email on signup, payment receipt email on successful purchase.
- **Step 25: Final QA pass** — every endpoint via Postman, every page via browser, both as USER and ADMIN, both with zero and non-zero credits.
- **Step 26: Switch to live keys** — `rzp_live_*` in both Railway and Vercel. No code changes.

---

## 4. WHY THIS ORDER

- **Backend first** — frontend has nothing meaningful to do without the API. Postman lets us verify each layer before any UI exists.
- **Webhook last (not during backend phase)** — webhooks need a public URL, a real frontend, AND a real Razorpay test transaction to be meaningfully tested. Hand-crafted curl payloads against a webhook endpoint don't reflect reality. Register the webhook only after a real test payment has succeeded end-to-end.
- **Deploy backend BEFORE building frontend** — so when we build the frontend, `VITE_API_URL` is real from day one. No "I'll wire it later" loose ends.
- **Postman at every backend step** — never move to step N+1 without confirming step N works in Postman.

---

## 5. HOW TO USE THIS DOC IN A NEW CHAT

Paste this template at the start of a new chat:

> I'm building CaptionAI. Two reference docs:
>
> 1. **Master Brief** — product spec, tech stack, DB schema, API endpoints, business rules: [paste full brief]
> 2. **Workflow** — locked decisions and build order: [paste this file]
>
> We're currently on **Step [N]: [name]**.
>
> [Then write your actual request for that step, e.g. "Build the pom.xml and entities for Step 1."]

One chat per phase is the sweet spot — phases are small enough to fit in context and big enough to maintain coherence. Don't mix phases in one chat.

---

## 6. ENV VARS REFERENCE (from brief)

### Backend — Railway
```
DATABASE_URL             = postgresql://user:password@host.neon.tech/captionai?sslmode=require
JWT_SECRET               = long random string, minimum 32 characters
GEMINI_API_KEY           = AIza...
RAZORPAY_KEY_ID          = rzp_test_...
RAZORPAY_KEY_SECRET      = ...
RAZORPAY_WEBHOOK_SECRET  = ...
RESEND_API_KEY           = re_...
FRONTEND_URL             = https://captionai.vercel.app
ADMIN_EMAIL              = your-admin@email.com
ADMIN_PASSWORD           = your-secure-admin-password
```

### Frontend — Vercel
```
VITE_API_URL             = https://captionai-backend.railway.app
VITE_RAZORPAY_KEY_ID     = rzp_test_xxxx   (swap to rzp_live_ for production)
```

For local dev: use a `.env` file (gitignored). `dotenv-java` reads it for the backend; Vite reads it natively for the frontend.

---

*CaptionAI Build Workflow — companion to Master Brief v4.2*
