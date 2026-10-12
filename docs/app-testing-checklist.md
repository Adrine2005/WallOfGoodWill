# 📱 Wall of Goodwill - Mobile App Testing Task List

This document provides a complete step-by-step testing checklist and verification guide for the **Wall of Goodwill** application, covering all user personas, core functionalities, and edge cases.

---

## 🔑 Pre-Seeded Credentials Reference

| Persona | Email | Password | Role | Campus Verified? | Starting Destination |
|---|---|---|---|---|---|
| **Campus Admin (Management)** | `admin@campus.edu` | `admin123` | `admin` | ✅ Yes | Home / Admin Dashboard |
| **Verified Student** | `student@campus.edu` | `student123` | `student` | ✅ Yes | Home (Main Feed) |
| **Unverified Student (Junior)** | `junior@campus.edu` | `junior123` | `student` | ⏳ No | Verify QR Notice Board Screen |
| **Physical Notice Board Code** | `GW-NOTICEBOARD-A` or `GW-31D63A7C` | N/A | Notice Poster | Valid for 1 Year | Unlocks Campus Verified Status |

---

## 📋 Comprehensive Testing Checklist

### 1. Authentication & Role Gateway
- [ ] **1.1 Admin / Management Login**
  - Enter `admin@campus.edu` and `admin123`.
  - Tap **Login**.
  - **Expected**: Successfully logs in and directly enters the main feed without being redirected to the Verify QR screen.
- [ ] **1.2 Verified Student Login**
  - Enter `student@campus.edu` and `student123`.
  - Tap **Login**.
  - **Expected**: Logs in directly to the active marketplace feed.
- [ ] **1.3 Unverified Student Login**
  - Enter `junior@campus.edu` and `junior123`.
  - Tap **Login**.
  - **Expected**: Lands on `VerifyQrActivity` ("Verify Campus Presence") prompting for hostel notice board code.
- [ ] **1.4 Non-Existent Account Error Handling**
  - Enter an uncreated email like `random123@univ.edu` with `password123`.
  - Tap **Login**.
  - **Expected**: Error banner displays: *"Account not found with this email. Please register first."*.
- [ ] **1.5 Wrong Password Error Handling**
  - Enter `student@campus.edu` with incorrect password `wrongpass`.
  - Tap **Login**.
  - **Expected**: Error banner displays: *"Invalid password. Please check your credentials."*.
- [ ] **1.6 Duplicate Email Registration Handling**
  - Navigate to Register screen and attempt registering with `student@campus.edu`.
  - **Expected**: Error banner displays: *"An account with this email already exists. Please login instead."*.
- [ ] **1.7 Notice Board Verification Flow**
  - While on the verification screen, type code `GW-NOTICEBOARD-A` (or `GW-31D63A7C`) and submit.
  - **Expected**: Verification success banner/toast appears and navigates to the marketplace feed with verified status unlocked.

---

### 2. Marketplace Feed & Item Discovery
- [ ] **2.1 View Seed Listings**
  - Verify seed items appear: Engineering Mathematics, Electric Kettle, Hercules Bicycle, LED Study Lamp, Drafter, Study Table.
  - Check that each card displays title, condition badge, price (or FREE DONATION), and seller hostel block.
- [ ] **2.2 Category Filter Chips**
  - Tap on horizontal category chips (`Books`, `Electronics`, `Cycles`, `Kitchenware`, `Stationery`, `Furniture`).
  - **Expected**: Feed filters instantly to items matching that category only.
- [ ] **2.3 Type Filter (Donate vs Sell)**
  - Tap "Donations (Free)" chip.
  - **Expected**: Only ₹0 donation items with green badges are shown.
- [ ] **2.4 Search Bar**
  - Search for keywords like "kettle" or "cycle".
  - **Expected**: Real-time filtering matching title and description.
- [ ] **2.5 Listing Detail Screen**
  - Tap on an item card.
  - **Expected**: Detail screen shows photo/vector fallback, seller name, hostel room details, full description, and contact actions.

---

### 3. Publishing Items (Create Listing)
- [ ] **3.1 Post a Free Donation**
  - Tap Floating Action Button (+) -> Select "Donation (Free)".
  - Verify Price field is automatically locked to 0.
  - Enter Title, Description, Category, Condition, and tap a quick photo preset chip.
  - Tap **Publish Listing**.
  - **Expected**: Created item appears immediately on top of the Home feed.
- [ ] **3.2 Post an Item for Sale**
  - Tap (+) -> Select "Sell at Price".
  - Enter Title, Price (e.g. ₹350), Condition, and Category.
  - Tap **Publish Listing**.
  - **Expected**: Card displays with price tag and correct rupee symbol.

---

### 4. Peer-to-Peer Donation Claim Flow
- [ ] **4.1 Submit Claim Request**
  - Open a donation listing posted by another student.
  - Tap "Request this Item" -> enter need message (e.g. *"Need this textbook for semester exams"*).
  - Submit request.
  - **Expected**: Request confirmation toast appears.
- [ ] **4.2 Donor Incoming Request Review**
  - Log in as the donor student -> Open "Donation Requests" from menu/navigation.
  - **Expected**: The incoming claim is visible with claimant name, room number, and message.
- [ ] **4.3 Donor Accepts Claim**
  - Tap **Accept** on the claim.
  - **Expected**:
    1. Request marked as "Accepted".
    2. Listing status changes to "DONATED".
    3. Claimant receives in-app notification.
    4. Donor is awarded +50 Goodwill Points.

---

### 5. In-App Notification Center
- [ ] **5.1 Notification Receipt**
  - Tap the Bell icon in the top toolbar.
  - **Expected**: Notification list shows *"Donation Request Accepted! 🎉"*.
- [ ] **5.2 Unread Badge Clearing**
  - Tap the notification or tap "Mark All as Read".
  - **Expected**: Unread indicator dot disappears.

---

### 6. Peer-to-Peer Item Chat
- [ ] **6.1 Open Item Conversation**
  - Open any listing detail screen -> tap "Chat with Seller".
  - **Expected**: Chat thread opens showing listing title in the app bar.
- [ ] **6.2 Send and Receive Messages**
  - Send message: *"Hi, when can I collect this from your room?"*.
  - **Expected**: Message bubble renders immediately with sender attribution and timestamp.

---

### 7. Campus Wishlist & Looking-For Board
- [ ] **7.1 Browse Wishlist Requests**
  - Tap "Wishlist" from navigation.
  - **Expected**: Student wishlist requests load with requester hostel block.
- [ ] **7.2 Post a Wishlist Item**
  - Tap (+) on the Wishlist screen.
  - Enter title (e.g. *"Engineering Drafter & Clips"*) and description.
  - **Expected**: Item immediately appears on the public wishlist board.

---

### 8. Goodwill Karma Points & Badges
- [ ] **8.1 Points Increment**
  - Open student Profile screen.
  - **Expected**: Balance reflects completed donations (+50 pts per accepted donation).
- [ ] **8.2 Milestone Badges**
  - Check Badges section.
  - **Expected**: "Campus Donor" badge is unlocked.
- [ ] **8.3 Campus Leaderboard**
  - Tap "View Leaderboard".
  - **Expected**: Student rankings ordered by points balance.

---

### 9. Community Moderation & Auto-Concealment
- [ ] **9.1 Report Listing**
  - Open any listing -> 3-dot overflow menu -> "Report Listing".
  - Select reason (e.g. *"Prohibited Item"*) and submit.
  - **Expected**: Report recorded successfully.
- [ ] **9.2 Auto-Concealment Trigger**
  - Submit additional reports to reach threshold (2 reports).
  - **Expected**: Listing status switches to `HIDDEN` and disappears from public feed.

---

## 🛠️ Management REST API Testing Commands

Run these in your Mac terminal while testing:

```bash
# Generate a new 24h hostel notice board QR code
curl -X POST http://localhost:8080/v1/admin/qr/generate \
  -H "Content-Type: application/json" \
  -d '{"label": "Hostel Block C Notice Board", "durationSeconds": 86400, "maxUses": 50}'

# List active QR codes
curl http://localhost:8080/v1/admin/qr/list

# Test Login endpoint directly
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@campus.edu", "password": "admin123"}'
```
