# Local verification

Install JDK 21 and set `JAVA_HOME` to its installation directory. Use the checked-in Maven wrapper; a separate Maven installation is not required.

On Windows PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\mvnw.cmd test
```

On macOS or Linux:

```sh
./mvnw test
```

The first run downloads dependencies. Tests use synthetic records and fake persistence, never a real Supabase project or credentials. Surefire is explicitly configured for JUnit 5 and fails if no tests are discovered. It runs tests on the classpath so the test framework can access package-private test classes without changing the application's module exports.

`ServiceRequestJsonTest` checks the outbound request's database field names and exact estimate. The pricing tests exercise catalog validation and request creation without a UI or backend.

The JavaFX controller tests require a graphical environment, even though they do not show application windows. On Linux CI, run `xvfb-run -a ./mvnw test`. To run only domain and serialization checks without a display, use `./mvnw '-Dtest=ServiceCatalogTest,ServiceRequestSubmissionTest,ServiceRequestJsonTest' test`.

## Pricing behavior

Services are identified by catalog UUID, so similarly named services remain distinct. The form uses a snapshot of the loaded catalog for both the displayed and saved estimate. Missing, negative, and fractional-cent prices are unavailable; a valid zero price is allowed. Failed loading blocks submission and offers retry. A failed save preserves the form and asks the customer to check existing requests before submitting again because a lost response does not prove the write failed.

The desktop estimate is not a server-side pricing guarantee. Server authorization, authoritative repricing, payment processing, and atomic job completion remain separate backend concerns.

## UI verification

Controller tests exercise real JavaFX controls with a deterministic background executor and fake data. They cover loading, retry, unavailable prices, selection totals, validation, pending submissions, failures, session changes, and navigation away from a pending operation. Login and dashboard smoke tests verify FXML loading, field labels, and role-specific action visibility.

The tests save scene snapshots under `target/ui-preview/` at 600 × 500, 800 × 600, and 1000 × 800, including selected services, loading failure, and the scrolled form. The README preview is copied from `request-selected-1000x800.png`; regenerate it after visual changes. Fixtures use synthetic people, vehicles, and workshops.

Verified locally with JDK 21: `mvnw.cmd clean package` completes with 40 tests, zero failures, errors, or skipped tests. JavaFX emits a classpath configuration warning in tests, and Shade reports overlapping dependency resources during packaging. Neither prevents this build; the test result does not assert that the packaged JAR runs on every platform.
