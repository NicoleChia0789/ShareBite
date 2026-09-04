# Progress

- Started merchant and global usability update.
- Completed scope inspection and identified shared navigation, quantity, and listing repository paths.
- Added merchant listing quantity/date filters, swapped merchant navigation icons, and changed the Charity label.
- Added comma-formatted quantity input handling and central expired-listing repository filtering.
- Formatting patch attempt 1 did not apply because an expected UserDashboard import line differed; no code was changed by that failed patch. Reapplying in smaller file-specific edits.
- Added shared display date-time normalization, state-preserving bottom navigation, and formatted initial values in Edit Request.
- Static checks completed. Final Gradle verification is not run because the prior cache-bypass build approval was rejected.
- Removed SplashScreen from the app flow while keeping Choose Role for registration.
- Confirmed Merchant dashboard My Listings cards are clickable and added previous-value initialization for Edit Request date/time pickers.
- Searched user-visible text for Chinese characters; remaining matches are comments/debug labels only.
- Added Gemini listing assistant Edge Function and Android service. Add Donation now generates English food name, description, category, and dietary type from the selected photo; Android compile passed. Supabase function still needs deployment before end-to-end testing.
