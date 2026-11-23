package equipmentReservation.model.admin;

import equipmentReservation.model.AbstractModel;
import equipmentReservation.model.AuthorizationException;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.function.Supplier;

/** Administrator settings for reservation pickups. */
public class SettingsService extends AbstractModel {
    private final SettingsStore store;
    private final Supplier<User> currentUser;

    public SettingsService(SettingsStore store, Supplier<User> currentUser) {
        this.store = Objects.requireNonNull(store);
        this.currentUser = Objects.requireNonNull(currentUser);
    }

    /** FR-15: Returns the saved pickup window, or 30 minutes if unset. */
    public int getPickupWindowMinutes() throws Exception {
        requireSignIn();
        Integer saved = store.getPickupWindowMinutes();
        return saved == null ? 30 : saved;
    }

    /** FR-15: Only an administrator can change the pickup window. */
    public void setPickupWindowMinutes(int minutes) throws Exception {
        User user = requireSignIn();
        if (!user.isAdmin()) {
            throw new AuthorizationException("Administrator access is required.");
        }
        if (minutes < 1 || minutes > 1440) {
            throw new ValidationException("pickupWindow",
                    "Pickup window must be between 1 and 1440 minutes.");
        }
        store.setPickupWindowMinutes(minutes);
        notifyChanged(new ModelEvent(this, ActionEvent.ACTION_PERFORMED, "SETTINGS"));
    }

    private User requireSignIn() throws AuthorizationException {
        User user = currentUser.get();
        if (user == null) {
            throw new AuthorizationException("Sign in is required.");
        }
        return user;
    }
}
