package equipmentReservation.controller;

import equipmentReservation.model.Model;
import equipmentReservation.view.View;

/**
 * A controller maps user gestures from its view to operations on the model (NFR-05).
 */
public interface Controller {

    /**
     * Sets the model this controller operates on.
     *
     * @param model the model
     */
    void setModel(Model model);

    /**
     * Returns the model this controller operates on.
     *
     * @return the model
     */
    Model getModel();

    /**
     * Returns the view this controller handles.
     *
     * @return the view
     */
    View getView();

    /**
     * Sets the view this controller handles.
     *
     * @param view the view
     */
    void setView(View view);
}
