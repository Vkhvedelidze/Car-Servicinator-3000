# Reliable service requests and desktop UI

Approved scope: implement the pricing and regression-test proposal, with a calm, professional UI refresh using Impeccable. All commits stay local; no GitHub push or live database changes.

## Implementation

1. Configure JUnit 5 execution in Maven, with a meaningful existing-model regression as the baseline.
2. Introduce an immutable catalog snapshot keyed by service UUID. Derive the displayed and submitted quote from the same validated selection. Missing, negative, or sub-cent prices are unavailable; valid zero prices remain selectable.
3. Test empty/failed catalogs, invalid or missing prices, exact decimal totals, duplicate names with distinct IDs, invalid submissions without writes, and successful request construction using fake persistence.
4. Replace fixed checkbox categories with catalog rows. Load catalog, vehicles, and shops on a background executor, with loading, empty, error, and retry states. Submit on a background executor and prevent duplicate clicks while pending. Guard callbacks when navigating away.
5. Refresh the request screen with a scrollable form and persistent estimate/action area. Align login and dashboard typography, spacing, and controls. Use scoped JavaFX CSS and keep native keyboard behavior.
6. Exercise FXML and request UI flows using fake data, including narrow windows and network failures. Run Maven tests and packaging, inspect the resulting diff, and make cohesive local commits.

## Acceptance

- Every selected service is resolved by its real ID and has a valid monetary price.
- A failed catalog load cannot produce a request or a misleading zero estimate.
- The saved estimate matches the displayed estimate exactly.
- Valid zero-price services work, and similarly named services remain distinct.
- Loading and submission do not block the JavaFX thread.
- All controls are reachable at 600 x 500 and common desktop sizes.
- Tests need no real Supabase project or credentials.

Database authorization, transactional completion/payment creation, and vehicle registration are separate follow-up contributions.

## Result

Implemented and verified with a clean package build: 40 tests pass with no failures, errors, or skips. Scene snapshots cover the refreshed login, dashboard, and request form, including narrow windows, selections, errors, and scrolling. Independent review identified navigation and session callback issues; these were corrected and covered by regressions. The README was also rewritten at the user's request with accurate setup, actual capabilities, known limitations, and a synthetic-data UI preview. Plaintext login credential logging was removed.
