package equipmentReservation.model.admin;

/** Reads and saves the reservation pickup window. */
public interface SettingsStore {
    Integer getPickupWindowMinutes() throws Exception;

    void setPickupWindowMinutes(int minutes) throws Exception;
}
