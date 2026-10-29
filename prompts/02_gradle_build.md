# INCREMENT 02: Gradle Build and Dependencies
**Status:** Standard · **Owner:** Sanskriti · **Dependencies:** 01 · **State:** ✅ DONE during repo setup (2026-10-29)

**CRITICAL: Gradle wrapper 8.14.3, Java 17, JUnit 5, sqlite-jdbc. `./gradlew build` MUST pass.**

---

### Capability
- `settings.gradle` sets `rootProject.name = 'equipment-reservation'`
- `build.gradle` contains:
  - plugins `java` and `application`; `mainClass = 'equipmentReservation.Main'`
  - Java 17 source and target
  - `implementation 'org.xerial:sqlite-jdbc:3.46.1.3'`
  - `testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'` and `testRuntimeOnly 'org.junit.platform:junit-platform-launcher'`
  - `test { useJUnitPlatform() }`, with test logging
  - `javadoc` with `memberLevel = PRIVATE` (course: all visibility scopes)
  - a `fatJar` task that bundles SQLite into one runnable jar (NFR-09)
- Gradle wrapper (`./gradlew`, `gradlew.bat`) committed
- `.gitignore` excludes `build/`, `.gradle/`, `*.db`, IDE files

### Verification
- [x] `./gradlew build` → BUILD SUCCESSFUL
- [x] `./gradlew dependencies --configuration runtimeClasspath` lists `org.xerial:sqlite-jdbc`

---
**CRITICAL: Do not add dependencies without a team decision (master context §7).**
