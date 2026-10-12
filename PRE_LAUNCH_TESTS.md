# 🚀 Wall of Goodwill: Pre-Launch Testing Handbook & Task List

This document is the official pre-launch testing checklist for the **Wall of Goodwill** campus marketplace and donation platform. Use this checklist to verify all student and management workflows before final deployment or viva presentation.

---

## 🔑 Pre-Seeded Testing Credentials

| Role / Persona | Email | Password | Campus Verified? | Destination on Login |
|---|---|---|---|---|
| **Campus Admin (Management)** | `admin@campus.edu` | `admin123` | ✅ Yes (`admin`) | **Home Feed** (Bypasses QR screen) |
| **Verified Student (Senior)** | `student@campus.edu` | `student123` | ✅ Yes (`student`) | **Home Feed** |
| **Unverified Student (Junior)** | `junior@campus.edu` | `junior123` | ⏳ No (`student`) | **Verify Campus QR Screen** |
| **Notice Board Poster Codes** | `GW-NOTICEBOARD-A` or `GW-31D63A7C` | N/A | Active for 1 Year | Unlocks campus verified status |

---

## 📋 Pre-Launch Verification Task List

### 1. Authentication & Security Gateway
- [ ] **1.1 Admin / Management Login**
  - Enter `admin@campus.edu` with password `admin123`.
  - Tap **Login**.
  - **Verify**: Logs in successfully and navigates directly to `MainActivity` without any redirect to `VerifyQrActivity`.
- [ ] **1.2 Verified Student Login**
  - Enter `student@campus.edu` with password `student123`.
  - Tap **Login**.
  - **Verify**: Successfully navigates to the public campus marketplace feed.
- [ ] **1.3 Unverified Student Login**
  - Enter `junior@campus.edu` with password `junior123`.
  - Tap **Login**.
  - **Verify**: Navigates to `VerifyQrActivity` ("Verify Campus Presence") asking to scan or enter a physical hostel notice board code.
- [ ] **1.4 Non-Existent Account Error Handling**
  - Enter an unregistered email `notregistered@univ.edu` with `password123`.
  - Tap **Login**.
  - **Verify**: Red error banner displays: *"Account not found with this email. Please register first."*.
- [ ] **1.5 Invalid Password Error Handling**
  - Enter `student@campus.edu` with incorrect password `wrongpass`.
  - Tap **Login**.
  - **Verify**: Red error banner displays: *"Invalid password. Please check your credentials."*.
- [ ] **1.6 Duplicate Registration Handling**
  - Tap **Register** and attempt signing up with `student@campus.edu`.
  - **Verify**: Red error banner displays: *"An account with this email already exists. Please login instead."*.
- [ ] **1.7 Notice Board Verification Flow**
  - From the verification screen, type `GW-NOTICEBOARD-A` (or `GW-31D63A7C`) and tap **Verify Code**.
  - **Verify**: Toast displays *"Campus verification successful"* and unlocks full marketplace access.

---

### 2. Marketplace Feed & Exploration
- [ ] **2.1 Seed Listings Feed**
  - Launch home feed and verify all 6 campus items load:
    1. Engineering Mathematics (Vol 1 & 2) — *FREE DONATION*
    2. Electric Kettle 1.5L — *₹450*
    3. Hercules Roadeo Geared Bicycle — *₹2,200*
    4. Rechargeable LED Desk Study Lamp — *FREE DONATION*
    5. Engineering Graphics Drafter & Tube — *FREE DONATION*
    6. Bedside Wooden Study Table — *₹550*
- [ ] **2.2 Category Filtering**
  - Tap category filter chips (`Books`, `Kitchenware`, `Cycles`, `Electronics`, `Stationery`, `Furniture`).
  - **Verify**: Feed updates to show only items belonging to that category.
- [ ] **2.3 Type Filtering (Donations vs Sales)**
  - Tap the **Donations** filter chip.
  - **Verify**: Feed displays only ₹0 donation items with green `FREE DONATION` badges.
- [ ] **2.4 Search Functionality**
  - Type *"kettle"* or *"cycle"* into the search box.
  - **Verify**: Items filter in real-time matching title and description.
- [ ] **2.5 Listing Detail Screen**
  - Tap an item card.
  - **Verify**: Detail screen displays photo/vector fallback, condition badge, seller name, hostel block, and room number.

---

### 3. Publishing Items (Create Listing)
- [ ] **3.1 Post a Free Donation**
  - Tap Floating Action Button (+) -> select **Donation (Free)**.
  - **Verify**: Price input is automatically locked to ₹0.
  - Enter Title, Description, select Category, select Condition, and tap a quick photo preset chip.
  - Tap **Publish Listing**.
  - **Verify**: The new donation item appears immediately on top of the Home feed.
- [ ] **3.2 Post an Item for Sale**
  - Tap (+) -> select **Sell at Price**.
  - Enter Title, custom Price (e.g., ₹300), Condition, Category, and Description.
  - Tap **Publish Listing**.
  - **Verify**: Item card renders with the rupee price tag.

---

### 4. Peer-to-Peer Donation Claim Flow
- [ ] **4.1 Submit a Claim Request**
  - While logged in as `junior@campus.edu`, open any donation item posted by `student@campus.edu`.
  - Tap **Request this Item** -> type message: *"Need this textbook for semester 3 exams"*.
  - Submit request.
  - **Verify**: Confirmation message displays: *"Request submitted to donor"*.
- [ ] **4.2 Donor Review**
  - Log in as donor `student@campus.edu`.
  - Open navigation menu -> tap **Donation Requests**.
  - **Verify**: The incoming claim is visible showing junior's name, hostel room, and message.
- [ ] **4.3 Donor Accepts Claim**
  - Tap **Accept**.
  - **Verify**:
    1. Request status updates to "Accepted".
    2. Listing status changes to `DONATED`.
    3. Claimant receives in-app notification.
    4. Donor is awarded +50 Goodwill Points.

---

### 5. In-App Notification Center
- [ ] **5.1 Real-Time Alert Delivery**
  - Tap the Bell icon in the top toolbar.
  - **Verify**: Notification displays: *"Donation Request Accepted! 🎉"*.
- [ ] **5.2 Clear Unread Badges**
  - Tap on the notification or tap **Mark All as Read**.
  - **Verify**: Unread indicator dot disappears.

---

### 6. Peer-to-Peer Item Chat
- [ ] **6.1 Open Conversation**
  - Open any listing detail screen -> tap **Chat with Seller**.
  - **Verify**: Chat room opens with listing title in top header.
- [ ] **6.2 Send and Receive Messages**
  - Type message: *"Hi, when can I collect this from your hostel room?"*.
  - Tap **Send**.
  - **Verify**: Chat bubble appears with sender name, content, and timestamp.

---

### 7. Campus Wishlist & Looking-For Board
- [ ] **7.1 Browse Wishlist Requests**
  - Tap **Wishlist** from navigation.
  - **Verify**: Active student wishlist requests load.
- [ ] **7.2 Post a Wishlist Item**
  - Tap (+) on the Wishlist screen.
  - Enter Title: *"Scientific Calculator fx-991ES"* with description.
  - Tap **Submit**.
  - **Verify**: Card immediately appears on the campus wishlist board.

---

### 8. Goodwill Karma Points & Badges
- [ ] **8.1 Points Increment**
  - Open student Profile screen.
  - **Verify**: Balance increments by +50 points per accepted donation.
- [ ] **8.2 Milestone Badges**
  - Check the Badges section on the profile.
  - **Verify**: *"Campus Donor"* badge is unlocked.
- [ ] **8.3 Campus Leaderboard**
  - Tap **View Leaderboard**.
  - **Verify**: Students are ranked by total points balance.

---

### 9. Community Moderation & Auto-Concealment
- [ ] **9.1 Report an Inappropriate Listing**
  - Open any listing -> 3-dot overflow menu -> **Report Listing**.
  - Select reason: *"Prohibited Item"* and submit.
  - **Verify**: Report confirmation toast appears.
- [ ] **9.2 Auto-Concealment Threshold**
  - Submit additional reports until threshold is reached (2 reports).
  - **Verify**: Listing status automatically switches to `HIDDEN` and disappears from public feeds.

---

## 🛠️ Management Backend Smoke Tests

When you start the Go backend (`go run ./cmd/api`), you can test all services from terminal:

```bash
# 1. Health & Uptime
curl -s http://localhost:8080/health | jq .

# 2. Test Admin Login
curl -s -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@campus.edu", "password": "admin123"}' | jq .

# 3. Test Student Login
curl -s -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "student@campus.edu", "password": "student123"}' | jq .

# 4. Generate New 24h Notice Board Code
curl -s -X POST http://localhost:8080/v1/admin/qr/generate \
  -H "Content-Type: application/json" \
  -d '{"label": "Hostel Block D Notice Board", "durationSeconds": 86400, "maxUses": 50}' | jq .

# 5. List Active Notice Board Codes
curl -s http://localhost:8080/v1/admin/qr/list | jq .
```
