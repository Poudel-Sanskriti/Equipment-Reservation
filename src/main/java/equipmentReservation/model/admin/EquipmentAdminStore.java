package equipmentReservation.model.admin;

import equipmentReservation.model.Condition;
import equipmentReservation.model.Equipment;
import java.time.Instant;
import java.util.List;

/** Stores equipment and its return-to-service history. */
public interface EquipmentAdminStore {
    Equipment find(String id) throws Exception;

    List<Equipment> findAll() throws Exception;

    void insert(Equipment equipment) throws Exception;

    void update(Equipment equipment) throws Exception;

    Equipment returnToService(String id, Condition condition, String adminUsername, Instant time)
            throws Exception;
}
