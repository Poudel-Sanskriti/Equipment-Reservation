package equipmentReservation.model.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A unit of database work that runs inside one transaction
 * (see {@link Database#inTransaction(TransactionWork)}, FR-37).
 *
 * @param <E> the checked exception the work may throw besides
 *            {@link SQLException}, for example a domain exception such as
 *            {@code ReservationConflictException}; use
 *            {@link RuntimeException} when there is none
 */
@FunctionalInterface
public interface TransactionWork<E extends Exception> {

    /**
     * Runs the work on a connection whose transaction is already open.
     * Do not commit, roll back or close the connection here.
     *
     * @param conn the connection to use
     * @throws SQLException if a database operation fails
     * @throws E            if the work fails for another reason
     */
    void run(Connection conn) throws SQLException, E;
}
