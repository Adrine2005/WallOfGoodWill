-- Wall of Goodwill: Initial PostgreSQL Schema for Supabase
-- Idempotent DDL with index optimizations

-- 1. Users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'student',
    hostel_block VARCHAR(128) NOT NULL DEFAULT '',
    room_no VARCHAR(64) NOT NULL DEFAULT '',
    phone VARCHAR(32) NOT NULL DEFAULT '',
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_via_code VARCHAR(64) NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 2. Campus Invites & QR Codes
CREATE TABLE IF NOT EXISTS campus_invites (
    code VARCHAR(64) PRIMARY KEY,
    label VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    max_uses INT NOT NULL DEFAULT 0,
    used_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Listings
CREATE TABLE IF NOT EXISTS listings (
    id VARCHAR(64) PRIMARY KEY,
    seller_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seller_name VARCHAR(255) NOT NULL,
    hostel_block VARCHAR(128) NOT NULL DEFAULT '',
    room_no VARCHAR(64) NOT NULL DEFAULT '',
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    category VARCHAR(64) NOT NULL,
    type VARCHAR(32) NOT NULL, -- 'DONATE' or 'SELL'
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    condition VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'SOLD', 'DONATED', 'DELETED', 'HIDDEN'
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_listings_seller ON listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_listings_status ON listings(status);
CREATE INDEX IF NOT EXISTS idx_listings_category ON listings(category);
CREATE INDEX IF NOT EXISTS idx_listings_type ON listings(type);

-- 4. Listing Photos
CREATE TABLE IF NOT EXISTS listing_photos (
    id BIGSERIAL PRIMARY KEY,
    listing_id VARCHAR(64) NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    photo_url TEXT NOT NULL,
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_listing_photos_listing ON listing_photos(listing_id);

-- 5. Wishlist Items
CREATE TABLE IF NOT EXISTS wishlist_items (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    category VARCHAR(64) NOT NULL,
    fulfilled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wishlist_user ON wishlist_items(user_id);
CREATE INDEX IF NOT EXISTS idx_wishlist_fulfilled ON wishlist_items(fulfilled);

-- 6. Donation Requests
CREATE TABLE IF NOT EXISTS donation_requests (
    id VARCHAR(64) PRIMARY KEY,
    listing_id VARCHAR(64) NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    requester_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    requester_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'REJECTED'
    note TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_donation_requests_listing ON donation_requests(listing_id);
CREATE INDEX IF NOT EXISTS idx_donation_requests_requester ON donation_requests(requester_id);

-- 7. Chat Conversations
CREATE TABLE IF NOT EXISTS conversations (
    id VARCHAR(64) PRIMARY KEY,
    listing_id VARCHAR(64) NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    buyer_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seller_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_message TEXT NOT NULL DEFAULT '',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_conversations_buyer ON conversations(buyer_id);
CREATE INDEX IF NOT EXISTS idx_conversations_seller ON conversations(seller_id);

-- 8. Chat Messages
CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(64) PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation ON messages(conversation_id);

-- 9. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);

-- 10. User Karma & Points
CREATE TABLE IF NOT EXISTS user_karma (
    user_id VARCHAR(64) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    points INT NOT NULL DEFAULT 0,
    tier_name VARCHAR(64) NOT NULL DEFAULT 'Newcomer Donor',
    donation_count INT NOT NULL DEFAULT 0
);

-- 11. User Badges
CREATE TABLE IF NOT EXISTS user_badges (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    icon VARCHAR(32) NOT NULL DEFAULT '🌱',
    awarded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_badges_user ON user_badges(user_id);

-- 12. Moderation Reports
CREATE TABLE IF NOT EXISTS moderation_reports (
    id VARCHAR(64) PRIMARY KEY,
    listing_id VARCHAR(64) NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    reporter_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Default Seed Data (Idempotent)
INSERT INTO users (id, name, email, password_hash, role, hostel_block, room_no, phone, verified)
VALUES (
    'usr-admin-default',
    'Campus Management (Admin)',
    'admin@campus.edu',
    'admin123',
    'admin',
    'Admin Office',
    'Room A-01',
    '+91 9876543210',
    TRUE
) ON CONFLICT (email) DO NOTHING;

INSERT INTO campus_invites (code, label, expires_at, max_uses, used_count, is_active)
VALUES 
    ('GW-NOTICEBOARD-A', 'Hostel Block A Main Notice Board', NOW() + INTERVAL '365 days', 1000, 0, TRUE),
    ('GW-31D63A7C', 'Campus Main Notice Board Poster', NOW() + INTERVAL '365 days', 1000, 0, TRUE)
ON CONFLICT (code) DO NOTHING;

