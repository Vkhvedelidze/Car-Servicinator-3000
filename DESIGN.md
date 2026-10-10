# Desktop workspace design

## Scope and scene

This is a product interface. Customers, mechanics, and administrators use a desktop computer in a normally lit home or workshop office; clear labels and legible estimates matter more than decoration. Login, dashboard, and service request creation share `workspace.css`. Other application screens retain their existing styling until explicitly updated.

The visual direction is calm, professional, and clear. A light, warm grey canvas reduces contrast between the application and its physical surroundings. Flat sections, native controls, and one restrained deep blue accent keep attention on decisions and actions. There are no decorative gradients, shadows, animation, emoji, or fabricated activity metrics.

## Color tokens

JavaFX CSS does not support OKLCH. These sRGB hex values are the implementation equivalents for a restrained palette of low chroma neutrals and a muted blue accent. They are shared through JavaFX looked-up colors on `.app-shell`.

| Token | Value | Use |
| --- | --- | --- |
| `-workspace-canvas` | `#f3f4f1` | Main workspace |
| `-workspace-surface` | `#fafbf8` | Toolbar, fields, estimate footer |
| `-workspace-ink` | `#253137` | Primary text |
| `-workspace-muted` | `#59656b` | Supporting text |
| `-workspace-line` | `#d5dcda` | Section and row separators |
| `-workspace-control-line` | `#8c999e` | Native control boundaries |
| `-workspace-accent` | `#355c73` | Primary action, focus, selected controls |
| `-workspace-accent-hover` | `#294c62` | Primary action hover |
| `-workspace-selected` | `#e7eff3` | Selected service row |
| `-workspace-error` | `#a03130` | Error text on `#f5e9e5` |
| `-workspace-success` | `#296243` | Success text on `#e6efe6` |

Informational text uses `#294e65` on `#e7eff3`. Primary button text uses `#f6f8f9`. Status messages name the issue or outcome in words; color is supplementary.

## Type and spacing

Use the platform's Segoe UI family, with system fallback where unavailable. Body and controls are 14 px; supporting text is 13 px; section headings are 18 px; page headings are 28 px. The total uses 24 px to remain readable without dominating the form. Weight is the second hierarchy cue.

The application toolbar uses 24 px horizontal padding. Request content uses 28 px padding, 28 px between sections, and 6–16 px within related groups. Form content is capped at 900 px. Login fields are capped at 380 px, dashboard actions at 480 px, and introductory prose at 650 px. Controls are at least 40 px high, with compact navigation at 34 px. Corner radii are limited to 3–5 px.

## Components and behavior

- **Application shell:** shared toolbar, quiet navigation, scrollable main region. Identity uses the existing product name.
- **Service request:** four numbered sections for vehicle/workshop, services, additional work, and notes. All content remains reachable through a native vertical scrollbar at a 600 × 500 window.
- **Catalog rows:** native checkbox, service name and optional description, and a right-aligned price. Selected rows have a soft background and checked control. Unavailable rows explain the missing price and cannot be selected. The controller supplies catalog data and states.
- **Estimate footer:** stays visible below the scroll area. It shows the estimate, selection count, submit action, and wrapping feedback. An unavailable estimate is never presented as a valid zero total.
- **Additional work:** native grouped radio buttons. A separately labeled budget field describes an amount additional to the service estimate and appears only for budget permission. Contacting the client before extra work is the default.
- **Login:** one compact column with explicit labels, Enter-to-submit, inline errors, and account creation as a secondary action.
- **Dashboard:** role-specific actions using the same button vocabulary. Invisible role actions are removed from layout by the controller.
- **Feedback:** shared `status-info`, `status-error`, and `status-success` classes with wrapping text. Retry is a local catalog action. `catalog-placeholder` provides a static skeleton treatment when a controller renders placeholders.

## Accessibility and implementation

FXML labels use `labelFor` bindings. The interface retains native keyboard traversal, checkbox/radio behavior, dropdown controls, and scrollbars. Focus is a visible outlined state. Selected state also has a native check mark; status color is accompanied by text. Notes and long descriptions wrap. Disabled actions use a reduced opacity and keep their labels readable.

The stylesheet is scoped beneath `.app-shell` and overrides the legacy button scaling, shadows, and saturated colors on these three screens. Do not introduce browser-only CSS properties. Keep asynchronous loading, retry, selection, budget validation, and submission behavior in the controller and services, not the FXML.
