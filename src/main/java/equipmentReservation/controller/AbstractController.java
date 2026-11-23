package equipmentReservation.controller;

import equipmentReservation.model.Model;
import equipmentReservation.view.View;

/**
 * Base class for every controller (NFR-05). Stores the controller's model
 * and view; subclasses implement {@link #operation(String)} to handle the
 * gestures sent by their view.
 */
public abstract class AbstractController implements Controller {

    /** The model this controller updates. */
    private Model model;

    /** The view this controller handles gestures for. */
    private View view;

    /**
     * Creates a controller with no model and no view; set them with
     * {@link #setModel(Model)} and {@link #setView(View)}.
     */
    protected AbstractController() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model getModel() {
        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setView(View view) {
        this.view = view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView() {
        return view;
    }

    /**
     * Handles a user gesture sent by the view, for example "search" or "reserve".
     *
     * @param option the name of the gesture
     */
    public abstract void operation(String option);
}
