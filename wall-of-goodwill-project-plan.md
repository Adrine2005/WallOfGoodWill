# Wall of Goodwill: Project Plan

A campus-only Android app where verified students can **donate** items for free or **sell** used items at a price they choose. Built solo, final-year minor project, on a zero-cost stack.

---

## 1. Idea in one paragraph

Hostel students often throw away or struggle to sell books, kitchen items, cycles, gadgets, and furniture, especially at move-out time. **Wall of Goodwill** gives them a trusted, campus-only place to list items either as a **Donation** (free) or a **Sale** (price set by the seller). Only authorised students can join. Buyers and receivers can search, filter by hostel block, request or chat with the seller, and meet on campus. No payments are handled in-app.

## 2. Hard constraints and decisions

| Constraint | Decision |
|---|---|
| No pay-as-you-go | Stay on **Firebase Spark (free)**. No Cloud Functions, no Firebase Storage. |
| Android language | **Java** |
| Build scripts | **Gradle Kotlin DSL** (`build.gradle.kts`) (assumed from "BCL in Kotlin"; change if you meant something else) |
| Server | **Go**, replaces Cloud Functions and handles anything privileged |
| Image storage | **Cloudinary free plan**, with uploads authorised by the Go server (see section 8) |
| Payments | None. Price is agreed in chat and handled in person. Optional UPI deep link later. |
| Working mode | Solo, AI-assisted |

> Free-tier limits and "needs billing" rules change. Before you start each phase, check the current Firebase pricing page, the Cloudinary pricing page, and your hosting provider's free plan. The numbers below are approximate.

## 3. Tech stack

| Layer | Choice | Why |
|---|---|---|
| Android app | Java, Material 3, ViewBinding, MVVM (ViewModel + LiveData), Navigation Component | Standard, well documented, easy for AI tools to help with |
| Networking | Retrofit + OkHttp (auth interceptor adds Firebase ID token) | Talks to the Go server |
| Image loading | Glide | Caching, placeholders, Cloudinary URLs |
| Image picking/compression | Android Photo Picker + `Bitmap` compress (or Compressor library) | Keep uploads small |
| Auth | Firebase Authentication (email/password + email verification) | Free on Spark |
| Database | Cloud Firestore | Real-time listeners, free quota |
| Push notifications | Firebase Cloud Messaging (FCM) | Free |
| Backend | Go (`net/http` with `chi` router) + Firebase Admin SDK for Go (`firebase.google.com/go/v4`) | Verifies tokens, sets claims, sends FCM, signs uploads, runs scheduled jobs |
| Image storage | Cloudinary (free) | No card needed; CDN and thumbnails built in |
| Monitoring | Firebase Crashlytics + Analytics | Free |
| Distribution | Firebase App Distribution (APK/AAB to testers) | Free; counts as deployment |
| Server hosting | See section 12 | Free options with trade-offs |

## 4. Architecture

```
+---------------------------+
|  Android app (Java)       |
|  MVVM + Retrofit + Glide  |
+------+------------+-------+
       |            |
  (1) Firebase   (2) HTTPS + ID token
   SDK direct         |
       |              v
       |      +-----------------+        +------------------+
       |      |   Go server     |------->|   Cloudinary     |
       |      | (REST API, cron)| signed |   (images)       |
       |      +--------+--------+ upload +------------------+
       |               | Admin SDK
       v               v
+---------------------------------------+
| Firebase: Auth | Firestore | FCM      |
+---------------------------------------+
```

**Rule of thumb**
- **Reads and real-time updates** (browse listings, chat messages): the app talks to Firestore directly. Fast and uses the SDK's caching.
- **Writes with side effects or trust requirements** (register, create/edit listing, donation requests, reports, admin actions): go through the **Go server**, which validates and then writes with the Admin SDK.
- **Firestore security rules** block direct client writes to sensitive collections, so nobody can bypass the server.

## 5. Users and roles

| Role | Can do |
|---|---|
| **Student** (verified) | Browse, post listings, request items, chat, wishlist, report |
| **Admin** | Everything above, plus manage whitelist, remove listings, suspend users, resolve reports |

### Authorised-student flow

1. Admin uploads a whitelist (college emails or roll numbers) into `allowedStudents` (via a simple admin screen or a one-time Go script reading a CSV).
2. Student signs up in the app with email and password, then verifies their email.
3. App calls `POST /v1/auth/register` with the Firebase ID token.
4. Go server checks the email against `allowedStudents`. If allowed, it creates the `users/{uid}` document and sets a **custom claim** `verified: true` (and `role: "admin"` for admins).
5. App refreshes the ID token. From now on Firestore rules check `request.auth.token.verified == true`.

If the email isn't whitelisted, registration is rejected and the account stays unusable.

## 6. Features by phase

### Phase 1: Core (must-have, demo-able on its own)
- Sign up / login / logout / forgot password, email verification
- Whitelist check and custom claims
- Create listing: title, description, category, type (Donate / Sell), price, condition, hostel block, up to 3 photos
- Home feed with real-time updates
- Search, plus filters (category, type, price range, hostel block)
- Listing detail screen
- My Listings: edit, delete, mark as Sold / Donated
- Profile screen

### Phase 2: Donation flow and notifications
- "Request this item" on donations, with a short message
- Donor sees requests and accepts one; others are auto-declined
- FCM push notifications: new request, request accepted, new message
- Wishlist keywords with a notification when a matching listing is posted
- Listing auto-expiry (e.g. 30 days)

### Phase 3: Chat and trust
- One-to-one chat per listing (Firestore subcollection)
- Report listing / user, block user
- Admin panel: review reports, remove listings, suspend users, manage whitelist

### Phase 4: Polish and extras
- Goodwill points and badges for donors (e.g. Bronze at 3 donations)
- "Move-out season" banner highlighting donations
- Dark mode, empty states, loading skeletons, error handling
- Optional UPI deep link on sale listings

> **Cut line:** if time runs short, drop Phase 4 first, then the admin panel UI (admin actions can be done via API calls), and keep chat as a simple "Contact seller" button if needed.

## 7. Firestore data model

Use flat, query-friendly collections. Store only what you need to display in lists, to save reads.

```
allowedStudents/{emailLower}
  email, name, rollNo, addedAt

users/{uid}
  name, email, hostelBlock, roomNo?, phone?, photoUrl?
  role            // "student" | "admin"
  goodwillPoints  // number
  fcmTokens       // array of device tokens
  suspended       // boolean
  createdAt

listings/{listingId}
  sellerId, sellerName, sellerBlock
  title, titleLower            // for prefix search
  description
  category                     // enum string
  type                         // "DONATE" | "SELL"
  price                        // 0 for donations
  condition                    // "NEW" | "LIKE_NEW" | "GOOD" | "FAIR"
  hostelBlock
  images: [ { url, thumbUrl, publicId } ]
  status                       // "ACTIVE" | "RESERVED" | "CLOSED" | "REMOVED"
  keywords: [ ... ]            // lowercase tokens for wishlist matching and search
  createdAt, updatedAt, expiresAt

requests/{requestId}            // donation requests
  listingId, donorId, requesterId, requesterName
  message
  status                       // "PENDING" | "ACCEPTED" | "DECLINED" | "CANCELLED"
  createdAt

chats/{chatId}                  // chatId = listingId_buyerId
  listingId, listingTitle, listingThumb
  participants: [uidA, uidB]
  lastMessage, lastMessageAt
  /messages/{messageId}
    senderId, text, createdAt

wishlists/{uid}
  keywords: [ ... ]

reports/{reportId}
  reporterId, targetType ("LISTING"|"USER"), targetId
  reason, details, status ("OPEN"|"RESOLVED"), createdAt

blocks/{uid}/blocked/{blockedUid}
  createdAt
```

### Indexes you will likely need (Firestore will prompt with a link)
- `listings`: `status` + `createdAt` (desc)
- `listings`: `status` + `category` + `createdAt` (desc)
- `listings`: `status` + `type` + `createdAt` (desc)
- `requests`: `listingId` + `status`

### Search note
Firestore has no full-text search. For a minor project use **prefix search on `titleLower`** (`>= q` and `<= q + '\uf8ff'`) plus category filters. Mention full-text search (Algolia/Typesense) as future work in your report.

## 8. Image and file storage plan

**Why not Firebase Storage?** New Firebase projects generally need the paid Blaze plan to use Cloud Storage. Since you want to avoid billing, use **Cloudinary's free plan** (no card needed at the time of writing; verify).

### Upload flow (signed uploads, no secrets in the app)
1. User picks up to 3 photos with the Photo Picker.
2. App **compresses** each image: max ~1280 px on the long edge, JPEG quality ~80, target under ~300 KB.
3. App calls `POST /v1/uploads/sign` (with ID token). The Go server verifies the user and returns a one-time **signature, timestamp, API key, cloud name, folder**.
4. App uploads the image directly to Cloudinary using those params (multipart POST). Cloudinary returns `secure_url` and `public_id`.
5. App sends the listing details plus image URLs and public IDs to `POST /v1/listings`.
6. Firestore stores only the URLs and IDs, never image bytes.

### Display
- Feed uses a thumbnail URL generated by Cloudinary's URL transformations (e.g. width 400, `q_auto`, `f_auto`). Detail screen uses a larger one.
- Glide caches them on-device.

### Cleanup
- When a listing is deleted or expires, the Go server calls Cloudinary's delete API with the stored `public_id`s to free quota.

### Profile photos
Optional. Use the same flow with a `profiles/` folder, or just use initials avatars to save quota.

### Backup option if Cloudinary doesn't work out
Store images on the Go server's disk and serve them from `/files/...`. This only works on hosts with a **persistent disk** (e.g. your own VM). Free hosts with ephemeral disks will lose files.

## 9. Go server design

### Folder structure
```
server/
  cmd/api/main.go
  internal/
    config/        // env loading
    auth/          // Firebase token verification middleware
    handlers/      // HTTP handlers
    services/      // business logic (listings, requests, notify, cloudinary)
    firebase/      // Admin SDK init, Firestore + FCM + Auth helpers
    jobs/          // expiry, cleanup
    models/
  Dockerfile
  go.mod
  .env.example
```

### Key packages
- `github.com/go-chi/chi/v5` for routing
- `firebase.google.com/go/v4` (Auth, Firestore, Messaging)
- `cloud.google.com/go/firestore`
- `github.com/cloudinary/cloudinary-go/v2` (or hand-roll the signature with SHA-1)
- `github.com/rs/cors`, `golang.org/x/time/rate` for CORS and rate limiting

### REST API (all under `/v1`, JSON, `Authorization: Bearer <Firebase ID token>`)

| Method | Path | Purpose |
|---|---|---|
| POST | `/auth/register` | Check whitelist, create user doc, set claims |
| POST | `/users/me/fcm-token` | Save/refresh device token |
| PATCH | `/users/me` | Update profile |
| POST | `/uploads/sign` | Return Cloudinary signed-upload params |
| POST | `/listings` | Create listing (validates, writes, runs wishlist matching) |
| PATCH | `/listings/{id}` | Edit (owner only) |
| POST | `/listings/{id}/status` | Mark sold/donated/closed |
| DELETE | `/listings/{id}` | Delete (owner or admin) and clean up images |
| POST | `/listings/{id}/requests` | Request a donation item, notifies donor |
| POST | `/requests/{id}/accept` | Donor accepts, others declined, requester notified |
| POST | `/notify/message` | After a chat message is written, server notifies the other participant |
| POST | `/reports` | Report a listing or user |
| POST | `/wishlist` | Update wishlist keywords |
| GET | `/admin/reports` | List open reports (admin) |
| POST | `/admin/listings/{id}/remove` | Remove listing (admin) |
| POST | `/admin/users/{uid}/suspend` | Suspend user (admin) |
| POST | `/admin/whitelist` | Add emails to whitelist (admin) |
| GET | `/health` | Health check (also used to wake the server) |

### Middleware
1. Request logging
2. CORS (only needed if you build a web admin panel)
3. Rate limiting per IP/user
4. Firebase token verification, attaching `uid`, `claims` to the request context
5. Role guard for `/admin/*`

### Background jobs (goroutines)
- **Expiry:** every hour, mark listings with `expiresAt < now` as `CLOSED` and delete their images.
- **Cleanup:** remove stale FCM tokens when FCM returns `unregistered`.

### Important: free hosting sleeps
Many free hosts spin the server down when idle, so timed jobs may not run reliably. Handle this by:
- Making expiry **also lazy**: the app filters out listings where `expiresAt` is in the past, so correctness doesn't depend on the job.
- Hitting `/health` when the app launches, to warm the server.

## 10. Firestore security rules (starting point)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function signedIn()  { return request.auth != null; }
    function verified()  { return signedIn() && request.auth.token.verified == true; }
    function isAdmin()   { return verified() && request.auth.token.role == 'admin'; }

    // Whitelist: server/admin only
    match /allowedStudents/{id} { allow read, write: if false; }

    match /users/{uid} {
      allow read: if verified();
      allow write: if false;               // server only
    }

    match /listings/{id} {
      allow read: if verified();
      allow write: if false;               // server only
    }

    match /requests/{id} {
      allow read: if verified() &&
        (resource.data.donorId == request.auth.uid ||
         resource.data.requesterId == request.auth.uid);
      allow write: if false;
    }

    match /chats/{chatId} {
      allow read: if verified() && request.auth.uid in resource.data.participants;
      allow create: if verified() && request.auth.uid in request.resource.data.participants;
      match /messages/{mid} {
        allow read: if verified() &&
          request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
        allow create: if verified() &&
          request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants &&
          request.resource.data.senderId == request.auth.uid;
      }
    }

    match /wishlists/{uid} {
      allow read, write: if verified() && request.auth.uid == uid;
    }

    match /reports/{id} { allow read, write: if false; }  // via server, admin reads via API
  }
}
```

Test these in the Firebase Emulator's Rules Playground and take screenshots for your report.

## 11. Android app plan

### Project setup
- `minSdk 24`, `targetSdk` latest stable, Java 17 compatible
- Gradle **Kotlin DSL** with a **version catalog** (`libs.versions.toml`)
- Firebase BoM, plus: Auth, Firestore, Messaging, Crashlytics, Analytics

### Package structure
```
com.yourname.goodwill
  ui/
    auth/        LoginActivity, RegisterFragment, VerifyEmailFragment
    home/        FeedFragment, FilterBottomSheet
    listing/     ListingDetailFragment, CreateListingFragment, MyListingsFragment
    requests/    RequestsFragment
    chat/        ChatListFragment, ChatFragment
    profile/     ProfileFragment
    admin/       AdminReportsFragment (visible to admins only)
  data/
    model/       Listing, User, Request, Message (POJOs with no-arg constructors for Firestore)
    remote/      ApiService (Retrofit), AuthInterceptor, CloudinaryUploader
    repository/  ListingRepository, AuthRepository, ChatRepository...
  util/          ImageCompressor, Constants, DateUtils
  MainActivity.java (hosts NavHostFragment + BottomNavigationView)
```

### Screens
1. Splash (checks auth state)
2. Login / Register / Verify email / Forgot password
3. Home feed with search bar and filter chips
4. Filter bottom sheet
5. Listing detail (image carousel, seller info, Request / Chat buttons)
6. Create / Edit listing (multi-image picker)
7. My listings
8. Requests (incoming and outgoing)
9. Chat list and chat screen
10. Wishlist
11. Profile (goodwill points, badges, settings)
12. Admin reports (admin only)

### Libraries (Java-friendly)
`firebase-bom`, `retrofit2`, `converter-gson`, `okhttp3 logging-interceptor`, `glide`, `material`, `navigation-fragment/ui`, `lifecycle-viewmodel/livedata`, `swiperefreshlayout`, `viewpager2` (image carousel).

### Key implementation notes
- **Auth interceptor:** call `FirebaseUser.getIdToken(false)` and attach as Bearer token; refresh on 401.
- **Pagination:** use Firestore `limit(20)` with `startAfter(lastDoc)`. This saves read quota.
- **Real-time listeners:** attach in `onStart`, detach in `onStop` to avoid wasted reads.
- **Offline:** Firestore persistence is on by default on Android; it helps with flaky hostel Wi-Fi.
- **Firestore POJOs:** need an empty constructor and getters/setters; use `@ServerTimestamp` for time fields.

## 12. Deployment plan (all free)

### Go server
| Option | Notes |
|---|---|
| **Render free web service** | Easy Docker or Go deploy from GitHub. Sleeps when idle (slow first request). Ephemeral disk. Good enough with Cloudinary. |
| **Oracle Cloud Always Free VM** | Always on with persistent disk, but signup needs card verification and capacity can be hard to get. |
| **Your laptop + Cloudflare Tunnel** | Free, fine for the final demo, not for ongoing use. |

Check each provider's current free plan before choosing. Keep the server stateless (all data in Firestore and Cloudinary) so you can move hosts easily.

Store secrets (Firebase service account JSON, Cloudinary API secret) as **environment variables** on the host. Never commit them to Git.

### Android app
1. Create a **signed release build** (keystore, R8 minification enabled).
2. Upload to **Firebase App Distribution** and invite testers (classmates, friends).
3. Enable **Crashlytics** and show a crash dashboard in your report.
4. Optional: Google Play Internal Testing (needs a one-time developer fee, so skip if avoiding cost).

### Firebase
- Deploy security rules and indexes with the Firebase CLI: `firebase deploy --only firestore`
- Keep rules and indexes in the repo (`firestore.rules`, `firestore.indexes.json`).

### Optional admin web panel
A small static page on **Firebase Hosting** (free) that calls the Go `/admin/*` endpoints. Only build this if time allows; the in-app admin screen is enough.

## 13. Suggested timeline (8 weeks, adjust to your deadline)

| Week | Goal |
|---|---|
| 1 | Finalise scope. Set up Firebase project, Android project, Go server skeleton, Git repos, Cloudinary account. Design UI wireframes. |
| 2 | Auth: sign-up, login, email verification, whitelist check, custom claims, security rules v1. |
| 3 | Create listing with image compression and Cloudinary signed upload. Go `/listings` endpoint. |
| 4 | Home feed, search, filters, detail screen, My Listings, mark sold. Deploy Go server. **Phase 1 demo-ready.** |
| 5 | Donation requests and accept flow. FCM notifications. Wishlist. |
| 6 | Chat, reports, block. Admin endpoints and screen. |
| 7 | Polish: expiry, goodwill points, dark mode, error and empty states, testing on real devices. |
| 8 | Release build, App Distribution, Crashlytics, documentation, report, presentation, demo rehearsal. |

## 14. Testing checklist
- [ ] Non-whitelisted email cannot register
- [ ] Unverified email cannot use the app
- [ ] User cannot edit or delete someone else's listing (try via API directly)
- [ ] Direct Firestore writes to `listings` from the client are denied
- [ ] Image upload works on slow network, and large images are compressed
- [ ] Donation accept flow: only one requester accepted, others declined
- [ ] Notifications arrive with app closed
- [ ] Expired listings disappear
- [ ] Suspended user is blocked
- [ ] App works after a cold-started (sleeping) server
- [ ] Test on at least 2 real devices and 2 Android versions

## 15. Risks and how to handle them

| Risk | Mitigation |
|---|---|
| Free Firestore quota exceeded | Pagination, listener hygiene, small documents, avoid reading whole collections |
| Free server sleeps | Warm-up call on app start, lazy expiry, demo with server pre-warmed |
| Cloudinary quota exceeded | Compress images, cap at 3 per listing, delete images of closed listings |
| Scope creep | Phase gating. Don't start a phase until the previous one works end to end |
| Spam or abuse | Whitelist-only access, rate limiting, report and block, admin removal |
| Solo workload | Build vertical slices (screen + endpoint + rules together), commit often |

## 16. Working with AI during development

- Give the AI **this file** plus the exact phase and feature you're building.
- Ask for **one vertical slice at a time**, e.g. "Create the Go handler for `POST /v1/listings` and the Retrofit interface and repository method in Java."
- Always ask the AI to **explain** the code, since you'll be asked about it in your viva.
- Don't paste secrets (service account keys, API secrets) into AI chats.
- Review generated security rules and auth checks yourself. Test them rather than trusting them.
- Keep a `DECISIONS.md` log of why you chose each tech. It makes your report much easier to write.

## 17. Report and viva checklist

- Problem statement and objectives
- Literature survey / comparison with OLX, Facebook groups, and WhatsApp groups (what is missing: verification, donation focus, campus scoping)
- System architecture diagram (section 4)
- ER / Firestore schema diagram (section 7)
- Use-case and sequence diagrams (registration, listing creation with image upload, donation accept)
- Security design: whitelist, custom claims, Firestore rules, server-only writes
- Screenshots of every screen
- Testing results (section 14)
- Deployment evidence: App Distribution, Crashlytics, hosted server
- Limitations and future work (full-text search, in-app payments, iOS, web app, AI-based item categorisation)

## 18. Immediate next steps

1. Create the Firebase project (Spark plan), enable Auth (email/password) and Firestore.
2. Create the Cloudinary account and note the cloud name, API key, and API secret.
3. Create two Git repos (or one monorepo): `android/` and `server/`.
4. Scaffold the Go server with `/health`, token-verification middleware, and `/auth/register`.
5. Scaffold the Android app with Firebase connected and the login screen working end to end.
6. Start Phase 1.
