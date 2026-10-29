package equipmentReservation.view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import equipmentReservation.controller.Controller;
import equipmentReservation.model.AbstractModel;
import equipmentReservation.model.Model;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.ModelListener;

/**
 * Base class for every application window. Registers itself with the model on
 * construction, as in the calculator example, and guarantees that
 * {@link #refresh(ModelEvent)} always runs on the Swing event dispatch thread,
 * even when the model fires from a background thread such as the expiration
 * scheduler (NFR-05).
 */
public abstract class JFrameView extends JFrame implements View, ModelListener {

    private static final long serialVersionUID = 1L;

    private transient Model model;
    private transient Controller controller;

    /**
     * Creates the view and registers it as a listener of the model.
     *
     * @param model      the model to render
     * @param controller the controller that handles this view's user gestures
     */
    public JFrameView(Model model, Controller controller) {
        setModel(model);
        setController(controller);
    }

    /** Registers this view as a listener of its model. */
    public void registerWithModel() {
        ((AbstractModel) model).addModelListener(this);
    }

    @Override
    public Controller getController() {
        return controller;
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public void setModel(Model model) {
        this.model = model;
        registerWithModel();
    }

    /**
     * Receives a model change and runs {@link #refresh(ModelEvent)} on the
     * event dispatch thread.
     *
     * @param event describes what changed
     */
    @Override
    public final void modelChanged(ModelEvent event) {
        if (SwingUtilities.isEventDispatchThread()) {
            refresh(event);
        } else {
            SwingUtilities.invokeLater(() -> refresh(event));
        }
    }

    /**
     * Updates the displayed data after a model change. Always called on the
     * event dispatch thread.
     *
     * @param event describes what changed
     */
    protected abstract void refresh(ModelEvent event);

    /**
     * Shows an error message in a dialog (NFR-04).
     *
     * @param message the user-facing message
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows an informational message in a dialog.
     *
     * @param message the user-facing message
     */
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Reports an invalid form field (NFR-04). The default shows the message in
     * a dialog; views with forms override this to highlight the field.
     *
     * @param field   the name of the invalid field
     * @param message the user-facing message saying how to correct it
     */
    public void showFieldError(String field, String message) {
        showError(message);
    }

    /** Closes the window and stops listening to the model. */
    @Override
    public void dispose() {
        if (model instanceof AbstractModel) {
            ((AbstractModel) model).removeModelListener(this);
        }
        super.dispose();
    }
}
