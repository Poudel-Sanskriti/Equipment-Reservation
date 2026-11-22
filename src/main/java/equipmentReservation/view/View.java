package equipmentReservation.view;

import equipmentReservation.controller.Controller;
import equipmentReservation.model.Model;

/**
 * A view in the push-MVC architecture (NFR-05). It shows the state of its
 * {@link Model} and sends user gestures to its {@link Controller}.
 */
public interface View {

    /**
     * Returns the controller that handles this view's gestures.
     *
     * @return the controller, or {@code null} if none is set
     */
    Controller getController();

    /**
     * Sets the controller that handles this view's gestures.
     *
     * @param controller the controller
     */
    void setController(Controller controller);

    /**
     * Returns the model this view shows.
     *
     * @return the model, or {@code null} if none is set
     */
    Model getModel();

    /**
     * Sets the model this view shows.
     *
     * @param model the model
     */
    void setModel(Model model);
}
