package equipmentReservation.controller;

import equipmentReservation.model.Model;
import equipmentReservation.view.View;

/**
 * Base class for all controllers: stores the model and view and declares the
 * {@link #operation(String)} dispatcher, as in the calculator example (NFR-05).
 * There is one controller class per view and one controller instance per view instance.
 */
public abstract class AbstractController implements Controller {

    private View view;
    private Model model;

    @Override
    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    /**
     * Performs the operation the user requested through the view.
     *
     * @param command the command constant of the UI component that was used
     */
    public abstract void operation(String command);
}
