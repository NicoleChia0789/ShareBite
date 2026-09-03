# Findings

- Merchant listings are displayed by `MerchantAllListingsScreen`; the backing repository already returns active listings only.
- Bottom navigation labels are computed in `BottomNavBar` and `TabletNavRail`; icons originate in `Screen.kt`.
- Charity quantity fields share `NumberInputUtils`; Merchant Add Donation uses a separate quantity string.
- Available listings are fetched by `FoodListingRepository` for user and charity browse screens; merchant dashboard uses `MerchantRepository`.
- Merchant listing filters now support quantity and pickup-date combinations. Merchant navigation labels/icons are updated at the shared `Screen` and navigation component level.
- Raw deadline output remains in user and charity detail/dashboard surfaces, so a shared date-time formatter will be used there.
- Shared display formatting now normalizes ISO-like `T` date-time values into a readable date and time where raw deadline output existed.
- Repository-level expired-listing filters cover user browse, charity browse, merchant active listings, and merchant all-listing retrieval.
- Bottom-nav navigation now saves the current destination state before switching tabs; back navigation continues to pop to the exact prior screen.
