# High-Concurrency Ticket Reservation Engine

A Spring Boot backend service engineered for high-traffic ticket reservations, focused on preventing overselling under concurrent requests, automatically reclaiming abandoned seat holds, and safely handling client retries.

## Tech Stack
- **Language & Framework:** Java, Spring Boot, Spring Data JPA (Hibernate)
- **Database:** PostgreSQL (Supabase)
- **Build & Tooling:** Maven, Git

## Architecture & Key Capabilities

### 1. Optimistic Locking for Seat Integrity (`@Version`)
Concurrent booking requests reading the same `availableSeats` count can cause race conditions or overselling if updates are not synchronized.
- Each `Event` entity includes a `@Version` field managed by Hibernate.
- During booking, updates validate the entity version (`WHERE id = ? AND version = ?`), causing conflicting concurrent transactions to fail safely (`ObjectOptimisticLockingFailureException`) rather than silently overwriting seat counts.

### 2. Reservation Lifecycle (`PENDING` → `CONFIRMED` / `EXPIRED`)
- **`PENDING`**: Created when a user books a ticket, decrementing `availableSeats` by `1` and setting an `expiresAt` timestamp.
- **`CONFIRMED`**: Transitioned when a reservation is confirmed prior to expiration.
- **`EXPIRED`**: Assigned to abandoned `PENDING` reservations once `expiresAt` has passed.

### 3. Automated TTL Seat Reclamation
Users frequently abandon checkout flows mid-way, which would permanently lock inventory without cleanup.
- `@EnableScheduling` activates a `@Scheduled` background worker (`ReservationCleanupService`).
- The worker periodically queries for `PENDING` reservations where `expiresAt` is in the past, updates their status to `EXPIRED`, and restores `availableSeats` within a transaction.

### 4. Idempotent Booking & State-Guarded Confirmation
- **Idempotency (`Idempotency-Key` header):** Retrying a booking request with the same `Idempotency-Key` returns the existing `TicketReservation` instead of creating a duplicate hold or deducting an extra seat.
- **Confirmation Validation:** Confirming a reservation checks that it is still in `PENDING` state and has not passed `expiresAt` before marking it `CONFIRMED` (and safely returns already-`CONFIRMED` reservations).

## API Endpoints

### Book a Ticket
`POST /api/events/{eventId}/book?userId={userId}`

- **Path Parameter:** `eventId` (`Long`) — Target event ID.
- **Query Parameter:** `userId` (`String`) — Identifier of the user placing the hold.
- **Optional Header:** `Idempotency-Key: <unique-key>` — Prevents duplicate bookings on network retries.
- **Behavior:** Checks idempotency key (if provided), verifies seat availability, decrements `availableSeats`, and creates a `PENDING` reservation with an expiration time.

### Confirm a Reservation
`POST /api/events/reservations/{reservationId}/confirm`

- **Path Parameter:** `reservationId` (`Long`) — ID of the reservation to finalize.
- **Behavior:** Confirms an active, unexpired `PENDING` reservation by setting its status to `CONFIRMED`.

## Configuration & Setup

1. **Configure Database Connection:**
   Set your PostgreSQL (Supabase) datasource properties in `src/main/resources/application.properties` (or via environment variables):
   ```properties
   spring.datasource.url=${DB_URL}
   spring.datasource.username=${DB_USER}
   spring.datasource.password=${DB_PASSWORD}
   spring.jpa.hibernate.ddl-auto=update