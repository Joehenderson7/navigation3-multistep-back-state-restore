1. [x] Align Kotlin Serialization plugin with Kotlin version: update kotlin("plugin.serialization") to 2.0.21 and keep versions centralized via version catalog.
2. [x] Migrate Room from kapt to KSP for faster builds: apply ksp plugin and replace kapt("androidx.room:room-compiler") with ksp("androidx.room:room-compiler").
3. [x] Move hardcoded dependency coordinates (serialization, datetime, datastore, Room, Mockito, coroutines-test) into gradle/libs.versions.toml for consistency.
4. [ ] Clean repo structure: remove or extract the nested duplicate project directory (navigation3-multistep-back-state-restore/) to avoid double Gradle projects and confusion.
5. [ ] Data model cleanup: remove the extra @Entity NukeGuage from StandardEntity.kt (misspelling and conflicting model). Use data.nuke.NukeGaugeEntity instead.
6. [ ] If a relation between gauges and standards is needed, model it via Room relations (@Relation with foreign keys) rather than embedding List<StandardEntity>; add a junction table if needed.
7. [ ] Add Room TypeConverters only when storing complex value objects is unavoidable; add unit tests for converters.
8. [ ] Refactor StandardRepository.insertStandard to avoid hardcoded gaugeSN = "3717"; pass it as a parameter or derive from PreferencesManager; rename fields for clarity (serialNumber vs gaugeSN).
9. [ ] Add repository-layer validation guards: non-empty serial number, positive density/moisture counts, sensible ranges; throw domain exceptions and cover with tests.
10. [ ] Introduce ViewModel layer for screens (Standards, Densities, Rices) to own state and business logic; stop creating repositories/databases inside composables.
11. [ ] Adopt unidirectional data flow (MVI-lite): define State, Intent, and Reducer in ViewModels; expose StateFlow to UI and process Intents.
12. [ ] Use SavedStateHandle in ViewModels to persist critical inputs (date, counts, serialNumber) across process death.
13. [ ] Hoist and share state where appropriate; reduce ad-hoc remember usage inside composables; use rememberSaveable only for UI entry fields.
14. [ ] Standardize input validation across the app: extract reusable validators/utils; apply isError and supportingText consistently; disable submit actions until valid.
15. [x] Replace java.util.Date/Calendar in repositories with kotlinx-datetime (Instant/LocalDate) and convert at UI boundaries; add tests for date range logic.
16. [ ] Introduce dependency injection (Hilt): provide Database, DAOs, Repositories, and PreferencesManager; wire ViewModels with @HiltViewModel.
17. [ ] Add robust error handling: map repository exceptions to UI error states; surface retry/snackbar actions; avoid crashes from coroutine failures.
18. [ ] Navigation 3: encapsulate tab/back navigation state in a single source of truth; add tests validating back behavior and state restoration.
19. [ ] Stabilize composables: mark immutable UI state data classes with @Immutable; use derivedStateOf for computed values to minimize recomposition.
20. [ ] Performance audit: split very large composables (e.g., Densities/Standards) into smaller, stable sub-composables; use LazyColumn for long lists.
21. [ ] Accessibility: add contentDescription for all icons and images; add semantics for interactive elements; test with TalkBack and Accessibility Scanner.
22. [ ] Internationalization: move hardcoded strings to resources; ensure numbers/dates respect Locale; cover formatting utilities with tests.
23. [ ] Theming: centralize TopAppBar color logic; verify dynamic color fallback; validate light/dark theme contrast compliance.
24. [ ] Room migrations: version the databases properly; add Migration implementations and tests with MigrationTestHelper.
25. [ ] DAO/Database tests: instrumented tests for each DAO covering queries, flows, and TypeConverters; use in-memory Room.
26. [ ] ViewModel unit tests: use kotlinx-coroutines-test and Turbine to verify StateFlow emissions and intent handling.
27. [ ] Compose UI tests: cover Standards and Densities screens for input validation, enabled/disabled Save/Next states, and list rendering.
28. [ ] Developer tooling: add a debug-only data seeding screen or dev menu; seed sample data for manual testing.
29. [ ] CI pipeline: add GitHub Actions workflow to run lint, static analysis, unit tests, and instrumented tests (connected) with Gradle caching.
30. [ ] Static analysis: integrate ktlint and detekt; configure rules; add a pre-commit hook; fix current style and smell violations.
31. [ ] Logging: add Timber; remove println; standardize log tags/levels and redact sensitive data.
32. [ ] R8/ProGuard: add keep rules for Room, DataStore, and kotlinx.serialization; verify a release build runs without reflection issues.
33. [ ] Crash safety: add a top-level CoroutineExceptionHandler where appropriate; ensure ViewModels handle failures and expose them as state.
34. [ ] Documentation: expand README with architecture overview, module boundaries, build/test instructions, and decisions; add ADRs for major changes (MVI, DI, KSP).
35. [ ] Code hygiene: remove dead code/unused previews; ensure remaining previews compile and cover varied states; consider @PreviewParameter for permutations.
36. [ ] Release management: adopt SemVer; update versionCode/versionName policies; add a CHANGELOG.md and release checklist.
37. [ ] Feature flags: guard Navigation 3 alpha usage behind a simple feature flag; document fallback/migration path.
38. [ ] Utilities: consolidate time/formatting helpers (e.g., convertMillisToDate) in a shared utils file; add unit tests.
39. [ ] Naming consistency: standardize on "NukeGauge" naming (fix NukeGuage typos in code/entities); add DB migration if schema is renamed.
40. [ ] Privacy and backup: audit DataStore keys and backup rules (res/xml/backup_rules.xml) to prevent leaking sensitive data; add encryption for sensitive prefs if needed.