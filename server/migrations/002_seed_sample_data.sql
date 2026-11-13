-- Wall of Goodwill: Seed Sample Data for Students, Listings, Photos, and Wishlist
-- Idempotent with ON CONFLICT DO NOTHING

-- 1. Sample Students (Users)
INSERT INTO users (id, name, email, password_hash, role, hostel_block, room_no, phone, verified, verified_via_code)
VALUES
    ('student-sample-1', 'Rohan Das', 'rohan@campus.edu', 'password123', 'student', 'Hostel Block A (Boys)', '302', '+91 9876543201', TRUE, 'GW-NOTICEBOARD-A'),
    ('student-sample-2', 'Priya Sharma', 'priya@campus.edu', 'password123', 'student', 'Hostel Block C (Girls)', '104', '+91 9876543202', TRUE, 'GW-NOTICEBOARD-A'),
    ('student-sample-3', 'Ankit Verma', 'ankit@campus.edu', 'password123', 'student', 'Hostel Block B (Boys)', '215', '+91 9876543203', TRUE, 'GW-NOTICEBOARD-A'),
    ('student-sample-4', 'Kavita Nair', 'kavita@campus.edu', 'password123', 'student', 'Hostel Block D (Girls)', '410', '+91 9876543204', TRUE, 'GW-NOTICEBOARD-A'),
    ('student-sample-5', 'Arun Kumar', 'arun@campus.edu', 'password123', 'student', 'Hostel Block A (Boys)', '112', '+91 9876543205', TRUE, 'GW-NOTICEBOARD-A'),
    ('student-sample-6', 'Sneha Patel', 'sneha@campus.edu', 'password123', 'student', 'Hostel Block C (Girls)', '305', '+91 9876543206', TRUE, 'GW-NOTICEBOARD-A')
ON CONFLICT (email) DO UPDATE SET
    name = EXCLUDED.name,
    hostel_block = EXCLUDED.hostel_block,
    room_no = EXCLUDED.room_no,
    phone = EXCLUDED.phone,
    verified = EXCLUDED.verified;

-- 2. Sample Listings
INSERT INTO listings (id, seller_id, seller_name, hostel_block, room_no, title, description, category, type, price, condition, status, created_at, updated_at)
VALUES
    (
        'lst-seed-001',
        'student-sample-1',
        'Rohan Das',
        'Hostel Block A (Boys)',
        '302',
        'Engineering Mathematics (Vol 1 & 2)',
        'Standard semester textbooks in great condition. Donating for free since I have completed the course.',
        'BOOKS',
        'DONATE',
        0.00,
        'GOOD',
        'ACTIVE',
        NOW() - INTERVAL '2 hours',
        NOW() - INTERVAL '2 hours'
    ),
    (
        'lst-seed-002',
        'student-sample-2',
        'Priya Sharma',
        'Hostel Block C (Girls)',
        '104',
        'Electric Kettle 1.5L',
        'Pigeon electric kettle, works perfectly. Great for instant noodles and late night tea/coffee.',
        'KITCHENWARE',
        'SELL',
        450.00,
        'LIKE_NEW',
        'ACTIVE',
        NOW() - INTERVAL '4 hours',
        NOW() - INTERVAL '4 hours'
    ),
    (
        'lst-seed-003',
        'student-sample-3',
        'Ankit Verma',
        'Hostel Block B (Boys)',
        '215',
        'Hercules Roadeo Geared Bicycle',
        'Moving out after final semester. Well maintained 21-speed cycle with bell and front basket.',
        'CYCLES',
        'SELL',
        2200.00,
        'GOOD',
        'ACTIVE',
        NOW() - INTERVAL '8 hours',
        NOW() - INTERVAL '8 hours'
    ),
    (
        'lst-seed-004',
        'student-sample-4',
        'Kavita Nair',
        'Hostel Block D (Girls)',
        '410',
        'Rechargeable LED Desk Study Lamp',
        '3 brightness settings with warm/cool lighting. Free giveaway for any junior.',
        'ELECTRONICS',
        'DONATE',
        0.00,
        'LIKE_NEW',
        'ACTIVE',
        NOW() - INTERVAL '12 hours',
        NOW() - INTERVAL '12 hours'
    ),
    (
        'lst-seed-005',
        'student-sample-5',
        'Arun Kumar',
        'Hostel Block A (Boys)',
        '112',
        'Engineering Graphics Drafter & Tube',
        'Mini drafter with scale, clamps, and waterproof drawing sheet carrier tube. Free donation.',
        'STATIONERY',
        'DONATE',
        0.00,
        'GOOD',
        'ACTIVE',
        NOW() - INTERVAL '16 hours',
        NOW() - INTERVAL '16 hours'
    ),
    (
        'lst-seed-006',
        'student-sample-6',
        'Sneha Patel',
        'Hostel Block C (Girls)',
        '305',
        'Bedside Wooden Study Table',
        'Foldable wooden laptop and study desk with cup holder. Ideal for small hostel rooms.',
        'FURNITURE',
        'SELL',
        550.00,
        'LIKE_NEW',
        'ACTIVE',
        NOW() - INTERVAL '20 hours',
        NOW() - INTERVAL '20 hours'
    )
ON CONFLICT (id) DO NOTHING;

-- 3. Listing Photos
INSERT INTO listing_photos (listing_id, photo_url, display_order)
VALUES
    ('lst-seed-001', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500', 0),
    ('lst-seed-002', 'https://images.unsplash.com/photo-1594213114663-ddf3f2a08126?w=500', 0),
    ('lst-seed-003', 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=500', 0),
    ('lst-seed-004', 'https://images.unsplash.com/photo-1534353436294-0dbd4bdac845?w=500', 0),
    ('lst-seed-005', 'https://images.unsplash.com/photo-1585776245991-cf89dd7fc73a?w=500', 0),
    ('lst-seed-006', 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=500', 0)
ON CONFLICT DO NOTHING;

-- 4. Sample Wishlist Items
INSERT INTO wishlist_items (id, user_id, user_name, title, description, category, fulfilled, created_at)
VALUES
    ('wsh-seed-001', 'student-sample-1', 'Rohan Das', 'Scientific Calculator (Casio FX-991EX)', 'Need for semester examination next week. Will return after exams or can buy.', 'ELECTRONICS', FALSE, NOW() - INTERVAL '1 day'),
    ('wsh-seed-002', 'student-sample-2', 'Priya Sharma', 'Hostel Room Floor Mat / Rug', 'Looking for a clean cotton or jute floor mat for room 104.', 'FURNITURE', FALSE, NOW() - INTERVAL '2 days'),
    ('wsh-seed-003', 'student-sample-4', 'Kavita Nair', 'Data Structures & Algorithms in Java Book', 'Looking for DSA textbook by Robert Lafore or Sedgewick.', 'BOOKS', FALSE, NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

-- 5. User Karma & Points
INSERT INTO user_karma (user_id, points, tier_name, donation_count)
VALUES
    ('student-sample-1', 120, 'Campus Patron', 2),
    ('student-sample-4', 90, 'Active Giver', 1),
    ('student-sample-5', 60, 'Kind Contributor', 1)
ON CONFLICT (user_id) DO UPDATE SET
    points = EXCLUDED.points,
    tier_name = EXCLUDED.tier_name,
    donation_count = EXCLUDED.donation_count;
