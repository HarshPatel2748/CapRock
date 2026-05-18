# CaptionAI — Master Project Brief v4
> Paste this entire document into any AI tool before asking it to build anything.
> It gives the AI full context about the product, design, database, and business logic.
> Code will be generated separately when needed.

---

## 1. WHAT THIS PRODUCT IS

CaptionAI is an Indian Micro-SaaS web app. It generates platform-aware social media captions from an uploaded image using Google Gemini 2.0 Flash (vision AI). The user uploads a photo, picks a platform (Instagram / X / LinkedIn), optionally selects a saved Brand Profile, and gets 3 caption styles back — Professional, Witty, and Minimalist — each with optimized hashtags.

The core insight: general AI tools like ChatGPT require the user to write a prompt every time. CaptionAI eliminates that friction by saving the user's brand voice once (Brand Profile) and applying it automatically on every generation.

**Target users:** Indian content creators, small business owners, lifestyle brands, D2C brands, social media managers.

**Monetisation:** Credit-based subscription via Razorpay. Users buy credits. 1 generation = 1 credit.

---

## 2. TECH STACK

| Layer | Tool | Notes |
|---|---|---|
| Frontend framework | React + Vite | Fast dev server, lightweight bundle |
| Styling | Tailwind CSS + Material UI + Headless UI + React Icons | See Section 3 for usage rules |
| HTTP client (frontend) | Axios | All API calls from React to Spring Boot |
| Backend framework | Spring Boot (Java) | REST API server, separate from frontend |
| Auth | Spring Security + JWT | JWT issued by Spring Boot |
| Database | PostgreSQL | Hosted on Railway |
| ORM | Spring Data JPA + Hibernate | Maps Java entities to PostgreSQL tables |
| AI | Google Gemini 3 Flash (`gemini-3-flash-preview`) | Vision + text, called from backend only |
| Payments | Razorpay | UPI, cards, netbanking, wallets |
| Email | Resend | Transactional emails — welcome, payment receipts |
| Frontend hosting | Vercel | Production-grade, used by thousands of SaaS products |
| Backend hosting | Railway (paid plan, $5/month) | Runs the Spring Boot JAR — paid plan prevents sleep on inactivity |
| Database hosting | Neon | Serverless PostgreSQL, free 10GB, never sleeps, better than Railway's PostgreSQL plugin for production |

---

## 3. STYLING RULES

The AI chooses the visual theme, colors, and aesthetic. Do not follow any specific color palette — let the AI make those creative decisions to produce something distinctive and beautiful.

### How to use the styling combo
- **Tailwind CSS** — all layout, spacing, flexbox, grid, responsive breakpoints, colors, borders. Primary styling tool.
- **Material UI (MUI)** — only for complex interactive components where the logic is expensive to rebuild: Dialog (modals), Snackbar (toasts), CircularProgress (loading spinner), Tooltip, Skeleton (loading placeholders). Import from `@mui/material`.
- **Headless UI** — accessible unstyled primitives: Transition (animations), Menu (dropdowns), Listbox (custom selects), Switch (toggles). Always styled with Tailwind on top. Import from `@headlessui/react`.
- **React Icons** — all icons throughout the app. Preferred sets: `ri` (Remix Icons) for UI icons, `si` (Simple Icons) for platform/brand logos.

### Mixing rules — important
- Do NOT use MUI's `sx` prop or `styled()` for layout — use Tailwind for that.
- Do NOT use MUI's TextField or Button — build those with Tailwind for full design control.
- Use MUI only where its component logic saves significant work (Dialog, Skeleton, Snackbar).

### Visual style direction
- Dark mode only — no light mode
- Modern, clean, premium SaaS aesthetic
- Smooth animations: cards fade and slide up on appear, skeleton shimmer for loading states
- Sidebar active item should be visually distinct from inactive items
- Hero section should have depth — gradients, subtle background texture or grid, visual hierarchy
- Cards should feel elevated and distinct from the page background
- Buttons should have clear hover states

---

## 4. PROJECT FOLDER STRUCTURE

### Frontend (React + Vite)
```
captionai-frontend/
├── public/
├── src/
│   ├── api/
│   │   └── axios.js              ← Axios instance with baseURL + JWT interceptor
│   ├── components/
│   │   ├── AuthModal.jsx          ← Sign in / Sign up modal
│   │   ├── CaptionCard.jsx        ← Output caption card
│   │   ├── CreditMeter.jsx        ← Sidebar credit progress bar
│   │   ├── UploadZone.jsx         ← Drag-and-drop image input
│   │   ├── Sidebar.jsx            ← Dashboard left navigation
│   │   └── Navbar.jsx             ← Landing page top nav
│   ├── pages/
│   │   ├── Landing.jsx            ← Public landing page
│   │   ├── admin/
│   │   │   └── AdminDashboard.jsx ← Admin-only panel (protected by ADMIN role)
│   │   └── dashboard/
│   │       ├── Layout.jsx         ← Dashboard shell with sidebar
│   │       ├── NewPost.jsx        ← Generate captions workspace
│   │       ├── Brands.jsx         ← Brand profiles CRUD
│   │       ├── Saved.jsx          ← Saved captions library
│   │       ├── Billing.jsx        ← Plans + Razorpay payment
│   │       └── Settings.jsx       ← Account settings
│   ├── context/
│   │   └── AuthContext.jsx        ← Global user state, JWT, credits, role
│   ├── hooks/
│   │   └── useAuth.js             ← useContext shorthand
│   ├── styles/
│   │   └── globals.css            ← CSS variables + base styles
│   ├── App.jsx                    ← Routes including /admin
│   └── main.jsx                   ← Entry point
├── index.html
├── vite.config.js
└── tailwind.config.js
```

### Backend (Spring Boot)
```
captionai-backend/
├── src/main/java/com/captionai/
│   ├── CaptionAiApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java        ← Spring Security + JWT filter chain
│   │   ├── CorsConfig.java            ← Allow Vercel frontend origin
│   │   └── JwtConfig.java             ← JWT secret, expiry, admin credentials
│   ├── controller/
│   │   ├── AuthController.java        ← /api/auth/register, /api/auth/login, /api/auth/me
│   │   ├── CaptionController.java     ← /api/generate
│   │   ├── BrandController.java       ← /api/brands CRUD
│   │   ├── SavedController.java       ← /api/saved CRUD
│   │   ├── PaymentController.java     ← /api/payment/create-order, /verify, /webhook
│   │   └── AdminController.java       ← /api/admin/** (ADMIN role only)
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── GeminiService.java         ← Calls Gemini 3 Flash API
│   │   ├── CreditService.java         ← Deduct, check, reset credits
│   │   ├── RazorpayService.java       ← Create order, verify signature
│   │   └── AdminService.java          ← User stats, revenue, usage data
│   ├── model/
│   │   ├── User.java
│   │   ├── BrandProfile.java
│   │   ├── SavedCaption.java
│   │   └── Payment.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── BrandProfileRepository.java
│   │   ├── SavedCaptionRepository.java
│   │   └── PaymentRepository.java
│   ├── dto/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java         ← contains JWT token + user info + role
│   │   ├── GenerateRequest.java
│   │   └── GenerateResponse.java
│   ├── security/
│   │   ├── JwtUtil.java               ← generate and validate JWT, embed role
│   │   ├── JwtFilter.java             ← intercept requests, validate token
│   │   └── UserDetailsServiceImpl.java
│   └── exception/
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   └── application.properties         ← all config loaded from env vars
└── pom.xml
```

---

## 5. DATABASE SCHEMA (PostgreSQL on Railway)

### Table: `users`

| Column | Type | Details |
|---|---|---|
| id | `BIGSERIAL` | Primary key, auto-increment |
| name | `VARCHAR(100)` | User's display name |
| email | `VARCHAR(150)` | Unique, not null |
| password_hash | `VARCHAR(255)` | BCrypt hashed, not null |
| plan | `VARCHAR(20)` | Default `'free'` — values: `free`, `starter`, `pro` |
| credits | `INTEGER` | Default `10` |
| credits_reset_at | `TIMESTAMPTZ` | Timestamp of last monthly credit reset |
| created_at | `TIMESTAMPTZ` | Default `NOW()` |

### Table: `brand_profiles`

| Column | Type | Details |
|---|---|---|
| id | `BIGSERIAL` | Primary key |
| user_id | `BIGINT` | Foreign key → `users(id)` ON DELETE CASCADE |
| name | `VARCHAR(100)` | Brand name, not null — e.g. "Pine & Pearls" |
| industry | `VARCHAR(100)` | e.g. "Handmade lifestyle" |
| vibe | `TEXT` | e.g. "aesthetic, cozy, warm" |
| audience | `TEXT` | e.g. "Women 22–35, lifestyle enthusiasts" |
| hashtags | `TEXT` | e.g. "#SlowLiving #HandmadeWithLove" |
| avoid | `TEXT` | e.g. "salesy language, exclamation marks" |
| created_at | `TIMESTAMPTZ` | Default `NOW()` |

### Table: `saved_captions`

| Column | Type | Details |
|---|---|---|
| id | `BIGSERIAL` | Primary key |
| user_id | `BIGINT` | Foreign key → `users(id)` ON DELETE CASCADE |
| caption_text | `TEXT` | The caption body, not null |
| hashtags | `TEXT` | Hashtags on a separate line |
| style | `VARCHAR(20)` | `Professional` / `Witty` / `Minimalist` |
| platform | `VARCHAR(30)` | `Instagram` / `X (Twitter)` / `LinkedIn` |
| created_at | `TIMESTAMPTZ` | Default `NOW()` |

### Table: `payments`

| Column | Type | Details |
|---|---|---|
| id | `BIGSERIAL` | Primary key |
| user_id | `BIGINT` | Foreign key → `users(id)` |
| amount | `INTEGER` | In paise — ₹99 = 9900, ₹299 = 29900 |
| plan | `VARCHAR(20)` | Plan purchased |
| razorpay_payment_id | `VARCHAR(100)` | From Razorpay response |
| razorpay_order_id | `VARCHAR(100)` | From Razorpay order creation |
| status | `VARCHAR(20)` | Default `'success'` |
| created_at | `TIMESTAMPTZ` | Default `NOW()` |

### Indexes
- `brand_profiles(user_id)`
- `saved_captions(user_id)`
- `payments(user_id)`

---

## 6. ADMIN LOGIN

There is only one admin. It is hardcoded — no admin table in the database.

**How it works:**
- Admin email and password are stored as environment variables: `ADMIN_EMAIL` and `ADMIN_PASSWORD`
- On the `/api/auth/login` endpoint, before checking the database, Spring Boot first checks if the incoming email matches `ADMIN_EMAIL` and the password matches `ADMIN_PASSWORD`
- If it matches → issue a JWT with `role: ADMIN` embedded in the claims. Do not look up the database at all.
- If it does not match → proceed with normal user login flow (check database, verify BCrypt hash, issue JWT with `role: USER`)
- The JWT always contains a `role` field: either `USER` or `ADMIN`

**Frontend behaviour:**
- After login, the frontend reads the `role` from the JWT or from the login response
- If `role === ADMIN` → redirect to `/admin`
- If `role === USER` → redirect to `/dashboard`
- The `/admin` route is protected — if a non-admin tries to access it directly, redirect them to `/dashboard`

**Admin dashboard shows:**
- Total users and signups over time
- Total generations (credits used) across all users
- Revenue summary from the `payments` table
- List of all users with their plan and credit count
- Ability to manually adjust a user's credits or plan

---

## 7. API ENDPOINTS

### Auth (public)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register with name, email, password. Returns JWT + user. |
| POST | `/api/auth/login` | Login. Checks admin credentials first, then DB. Returns JWT + role. |
| GET | `/api/auth/me` | Get current user info. Protected. |

### Captions (protected — USER role)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/generate` | Generate captions from image. Costs 1 credit. Returns raw Gemini text. |

### Brand Profiles (protected — USER role)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/brands` | List all brand profiles for logged-in user. |
| POST | `/api/brands` | Create a new brand profile. |
| PUT | `/api/brands/{id}` | Update a brand profile (must belong to user). |
| DELETE | `/api/brands/{id}` | Delete a brand profile (must belong to user). |

### Saved Captions (protected — USER role)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/saved` | List all saved captions, newest first. |
| POST | `/api/saved` | Save a caption. |
| DELETE | `/api/saved/{id}` | Delete a saved caption. |

### Payments (protected + public webhook)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/payment/create-order` | Creates a Razorpay order. Returns orderId + amount. |
| POST | `/api/payment/verify` | Verifies Razorpay signature. Updates plan + credits. |
| POST | `/api/payment/webhook` | Public. Razorpay calls this for monthly renewals. |

### Admin (protected — ADMIN role only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/stats` | Total users, total generations, total revenue. |
| GET | `/api/admin/users` | List all users with plan and credit info. |
| PATCH | `/api/admin/users/{id}` | Manually update a user's plan or credits. |

---

## 8. BUSINESS LOGIC RULES

### Credits
- Default on signup: 10 credits (free plan)
- 1 generation = 1 credit, deducted server-side in Spring Boot
- Credits checked server-side before calling Gemini — return HTTP 402 if zero
- Show warning banner in dashboard UI when credits ≤ 5
- Disable generate button when credits = 0
- Free plan credits never reset
- Starter and Pro credits reset monthly when Razorpay renewal webhook fires

### Plans
| Plan | Price | Credits | Resets | Brand Profiles | Saved Captions |
|---|---|---|---|---|---|
| Free | ₹0 | 10 lifetime | Never | 0 | No |
| Starter | ₹99/month | 50/month | Monthly | Max 3 | Yes |
| Pro | ₹299/month | 200/month | Monthly | Unlimited | Yes |

### Brand profile limits (enforced in service layer)
- Free → 0 brand profiles allowed (feature locked, show upgrade prompt)
- Starter → max 3 brand profiles
- Pro → unlimited brand profiles

### JWT
- Issued by Spring Boot on login and register
- Expiry: 24 hours
- Contains: `userId`, `email`, `role` (`USER` or `ADMIN`)
- Format: `Bearer <token>` in the `Authorization` header
- All routes except `/api/auth/**` and `/api/payment/webhook` are protected
- Frontend stores token in `localStorage` under key `cai_token`

---

## 9. PRIVACY & IMAGE HANDLING RULES

**This is a core product promise. Never violate it in any part of the code.**

- Images are converted to base64 in the browser only
- The base64 string is sent in the POST body to `/api/generate`
- Spring Boot passes it directly to Gemini and immediately discards it
- Images are NEVER saved to PostgreSQL, any file system, or object storage
- The server holds the image in memory only for the duration of the HTTP call
- Show this message permanently in the upload zone UI: `"🔒 Your image is processed and immediately discarded. Never saved."`
- This message also appears in the landing page privacy section

---

## 10. GEMINI PROMPT BEHAVIOUR

- Model: `gemini-3-flash-preview`
- Called from Spring Boot backend only — API key never exposed to frontend
- Input: base64 image + text prompt built on the server
- Output: raw text with 3 sections tagged `[PROFESSIONAL]`, `[WITTY]`, `[MINIMALIST]`
- Each section has caption text on one set of lines and hashtags on a separate line
- Frontend parses the raw text to split caption and hashtags per style
- Do NOT set temperature — Gemini 3 Flash is optimized for its default of 1.0; setting it lower causes degraded performance and looping
- Platform rules injected into prompt: Instagram (long, many hashtags, emojis ok), X/Twitter (max 280 chars, 2–3 hashtags), LinkedIn (professional, minimal emojis)
- Brand profile fields injected when selected: vibe, audience, hashtags to include, words to avoid

---

## 11. LANDING PAGE SECTIONS (in order)

1. **Navbar** — Logo left, Sign in + Start free buttons right. Fixed, blur backdrop on scroll.
2. **Hero** — Headline: "Stop Writing. Start Posting." Subheadline about brand voice in seconds. Two CTAs: "Start Generating for Free" and "See how it works". Three inline trust notes: "10 free credits · No credit card needed · Images never stored."
3. **Trust bar** — Platform icons: Instagram, X / Twitter, LinkedIn, YouTube (coming soon), Threads (coming soon).
4. **How it works** — 3 numbered cards: Upload image / AI applies brand / Copy and post.
5. **Privacy section** — 4 cards: Zero image storage / Brand data stays yours / Razorpay handles payments / API key hidden on server.
6. **Pricing** — 3 columns: Free / Starter ₹99 / Pro ₹299. Note below: "All plans paid via Razorpay · UPI · Cards · Netbanking · Wallets."
7. **Footer** — Logo, copyright, Privacy / Terms / Contact links.

---

## 12. DASHBOARD LAYOUT

```
┌─────────────────────────────────────────────────────┐
│ SIDEBAR (218px)          │ MAIN CONTENT (flex: 1)   │
│                          │                          │
│ [Logo]                   │ [Page header]            │
│                          │                          │
│ Workspace                │ [Page content]           │
│  + New post              │                          │
│  ◈ Brand profiles        │                          │
│  ♡ Saved captions        │                          │
│                          │                          │
│ Account                  │                          │
│  💳 Billing              │                          │
│  ⚙ Settings             │                          │
│  ← Back to site          │                          │
│                          │                          │
│ [User avatar + name]     │                          │
│ [Plan name]              │                          │
│ [Credit progress bar]    │                          │
│ [Upgrade link if ≤ 5]    │                          │
└─────────────────────────────────────────────────────┘
```

---

## 13. NEW POST WORKSPACE LAYOUT

```
┌──────────────────────┬──────────────────────────────┐
│ INPUT PANEL (340px)  │ OUTPUT PANEL (flex: 1)       │
│                      │                              │
│ 🔒 Privacy note      │ Empty state before generate  │
│                      │                              │
│ Image upload zone    │ MUI Skeleton cards during    │
│ (drag and drop)      │ generation                   │
│                      │                              │
│ Platform dropdown    │ ┌──────────────────────────┐ │
│                      │ │ PROFESSIONAL badge       │ │
│ Brand profile        │ │ Caption text             │ │
│ dropdown             │ │ #hashtags                │ │
│                      │ │ 142 chars  [Save][Copy]  │ │
│ Key detail textarea  │ └──────────────────────────┘ │
│ (optional)           │                              │
│                      │ ┌──────────────────────────┐ │
│ Style note input     │ │ WITTY badge              │ │
│ (optional)           │ │ ...                      │ │
│                      │ └──────────────────────────┘ │
│ Credit warning       │                              │
│ banner (if ≤ 5)      │ ┌──────────────────────────┐ │
│                      │ │ MINIMALIST badge         │ │
│ Generate button      │ │ ...                      │ │
│ "Uses 1 credit"      │ └──────────────────────────┘ │
└──────────────────────┴──────────────────────────────┘
```

---

## 14. ENVIRONMENT VARIABLES

### Frontend — Vercel
```
VITE_API_URL             = https://captionai-backend.railway.app
VITE_RAZORPAY_KEY_ID     = rzp_test_xxxx   (swap to rzp_live_ for production)
```

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

---

## 15. DEPLOYMENT

### Database → Neon
1. Go to neon.tech → Create a new project → Name it `captionai`
2. Neon creates a PostgreSQL database and gives you a connection string
3. Copy the connection string — add it as `DATABASE_URL` in Railway

### Backend → Railway
1. Push Spring Boot project to GitHub
2. Railway → New Project → Deploy from GitHub repo
3. Do NOT add a PostgreSQL plugin — Neon is the database
4. Add all env vars in Railway's Variables tab
5. Upgrade to Railway's paid plan ($5/month) so the server never sleeps on inactivity
6. Railway builds using Maven and runs the JAR automatically
7. Copy the generated Railway domain — use it as `VITE_API_URL` in Vercel

### Frontend → Vercel
1. Push React + Vite project to GitHub
2. Vercel → New Project → Import repo
3. Framework preset: Vite
4. Add `VITE_API_URL` and `VITE_RAZORPAY_KEY_ID` in environment variables
5. Deploy — Vercel runs `vite build` and serves `dist/`

### Going live with real payments
- Swap `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` to `rzp_live_` in Railway
- Swap `VITE_RAZORPAY_KEY_ID` to `rzp_live_` in Vercel
- No code changes needed

### Scaling beyond Railway (when you outgrow it)
- Move Spring Boot to **Render** (auto-scaling, $7/month starter) or **AWS Elastic Beanstalk**
- Neon scales automatically — no migration needed
- Vercel scales automatically

---

## 16. HOW TO USE THIS BRIEF

**Always paste this entire brief first. Then make your specific request.**

Examples:
- *"Build the React landing page using this brief."*
- *"Build the Dashboard layout and sidebar using this brief."*
- *"Build the NewPost.jsx component using this brief."*
- *"Build the Brands.jsx page with CRUD using this brief."*
- *"Build the Billing.jsx page with Razorpay using this brief."*
- *"Build the Settings.jsx page using this brief."*
- *"Build the Spring Boot AuthController with hardcoded admin login using this brief."*
- *"Build the Spring Boot CaptionController using this brief."*
- *"Build the Spring Boot PaymentController with Razorpay using this brief."*
- *"Build the AdminController and AdminDashboard.jsx using this brief."*
- *"Build the PostgreSQL schema for all tables using this brief."*
- *"Build the SecurityConfig and JWT setup with role support using this brief."*

---

*CaptionAI Master Brief — v4.2*
*Stack: React + Vite · Spring Boot · PostgreSQL on Neon · JWT · Razorpay · Gemini 3 Flash · Resend · Vercel + Railway*
