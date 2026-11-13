# Tasks

## 1. Client-Side Image Downscaling & Optimization

- [ ] 1.1 Implement in-memory downscaling and JPEG compression in `CreateListingActivity` (max 1280px dimension, ~82% quality) and verify output file size is under 500KB
- [ ] 1.2 Wire the optimized image generation into the asynchronous upload task and verify upload completes in under 500ms

## 2. Resilient Glide Network Pipeline

- [ ] 2.1 Update OkHttp DNS fallback in `ApiClient` to resolve Supabase Cloud Storage domains (`*.supabase.co`) to known edge IPs (`172.64.149.246`, `104.18.38.10`)
- [ ] 2.2 Configure Glide to use the resilient OkHttpClient for all image network requests and verify Supabase images load successfully in `ListingsAdapter` and `ListingDetailActivity`

## 3. Direct Item Deletion on Listing Detail

- [ ] 3.1 Add "Delete Listing" button to `activity_listing_detail.xml` layout and bind visibility conditionally when current authenticated user matches seller ID
- [ ] 3.2 Wire delete confirmation dialog and ViewModel deletion call in `ListingDetailActivity`, verifying that successful deletion navigates back to feed with updated listing state

## 4. End-to-End Flow Verification

- [ ] 4.1 Execute end-to-end item publishing flow in the Android app and verify fast image upload and photo rendering in the home feed
- [ ] 4.2 Execute item deletion flow on the created listing and verify removal from both client feed and Supabase PostgreSQL backend
