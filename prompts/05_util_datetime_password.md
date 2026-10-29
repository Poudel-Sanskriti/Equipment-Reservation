# INCREMENT 05: Utilities (DateTimes, PasswordHasher)
**Status:** Standard · **Owner:** Sanskriti · **Dependencies:** 04 · **Requirements:** FR-39, NFR-08

**CRITICAL: `util` depends on the JDK only. The display format is exactly `yyyy-MM-dd HH:mm`. Passwords use salted PBKDF2, never plaintext.**

---

### Capability
- `DateTimes`: `format`, `parse` (strict: rejects `2026-02-30 10:00` and `2026-10-12 9:00`; uses `ResolverStyle.STRICT` with pattern `uuuu-MM-dd HH:mm`), `toDb`, `fromDb`. `parse` throws `IllegalArgumentException("Use the format YYYY-MM-DD HH:mm, for example 2026-10-12 09:00.")`.
- `PasswordHasher`: `PBKDF2WithHmacSHA256`, 65,536 iterations, 256-bit key, 16-byte `SecureRandom` salt, Base64. `verify` uses `MessageDigest.isEqual`. Zero the char arrays you create internally.

### Tests to write FIRST (`test/DateTimesTest.java`, `test/PasswordHasherTest.java`)
- format/parse round trip; null → ""; invalid dates and wrong formats rejected; `toDb` strings sort chronologically.
- hash is not equal to the password; the same password with a different salt gives a different hash; verify true/false; an empty password still hashes.

### Verification
- [ ] Tests green; no imports from `equipmentReservation.*` inside `util`

---
**CRITICAL: Strict date parsing with friendly messages; salted, slow hashing; util stays dependency-free.**
