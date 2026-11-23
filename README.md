# Equipment Reservation and Checkout System

CS 4398 · Fall 2026 · Group 9: Sanskriti Poudel, Thomas Platt, Theo Kliewer

## The problem

University departments and libraries share equipment such as laptops, cameras, projectors and calculators. When reservations, checkouts, returns and maintenance are tracked in separate or informal records, nobody has one reliable view of what is available, who has it, and what condition it is in, and the same item can end up promised to two people.

This project is a Java desktop application for one organization that keeps all of that in one place and enforces the rules:

- **Borrowers** search the catalog, reserve an item for a time period, cancel, and see their history.
- **Administrators** manage equipment, check items out and back in, and put items under maintenance.
- An item can never be double-booked, even when two requests arrive at the same time.
- Reservations that are not picked up within the pickup window expire automatically.
- Items under maintenance cannot be reserved or checked out.

Built with Java 17, Swing, the MVC pattern, a local SQLite database, and JUnit 5 tests.

## Build and test

Requires **Java 17 or newer**. Nothing else needs to be installed; the Gradle wrapper downloads Gradle and the dependencies.

```bash
./gradlew build      # compile and run all tests
./gradlew test       # run the tests only
./gradlew javadoc    # generate the API docs into build/docs/javadoc
```

On Windows, use `gradlew.bat` instead of `./gradlew`.
