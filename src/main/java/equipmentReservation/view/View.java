package equipmentReservation.view;

import equipmentReservation.controller.Controller;
import equipmentReservation.model.Model;

/**
 * A view renders model data and forwards user gestures to its controller (NFR-05).
 */
public interface View {

    /**
     * Returns the controller that handles this view's user gestures.
     *
     * @return the controller
     */
    Controller getController();

    /**
     * Sets the controller that handles this view's user gestures.
     *
     * @param controller the controller
     */
    void setController(Controller controller);

    /**
     * Returns the model this view renders.
     *
     * @return the model
     */
    Model getModel();

    /**
     * Sets the model this view renders and registers with it.
     *
     * @param model the model
     */
    void setModel(Model model);
}
