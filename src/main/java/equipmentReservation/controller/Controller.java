package equipmentReservation.controller;

import equipmentReservation.model.Model;
import equipmentReservation.view.View;

/**
 * A controller in the push-MVC architecture (NFR-05). It receives user
 * gestures from its {@link View} and turns them into operations on its
 * {@link Model}. Each view has exactly one controller.
 */
public interface Controller {

    /**
     * Sets the model this controller updates.
     *
     * @param model the model
     */
    void setModel(Model model);

    /**
     * Returns the model this controller updates.
     *
     * @return the model, or {@code null} if none is set
     */
    Model getModel();

    /**
     * Sets the view this controller handles gestures for.
     *
     * @param view the view
     */
    void setView(View view);

    /**
     * Returns the view this controller handles gestures for.
     *
     * @return the view, or {@code null} if none is set
     */
    View getView();
}
