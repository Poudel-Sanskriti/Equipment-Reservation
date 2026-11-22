package equipmentReservation.view;

import equipmentReservation.controller.Controller;
import equipmentReservation.model.AbstractModel;
import equipmentReservation.model.Model;
import equipmentReservation.model.ModelListener;
import javax.swing.JFrame;

/**
 * Base class for every Swing window (NFR-05). A {@code JFrameView} is a
 * {@link JFrame} that is also a {@link View} and a {@link ModelListener}:
 * it registers itself with its model when constructed, so it is told about
 * every change. Subclasses implement {@link #modelChanged} to refresh the
 * screen.
 */
public abstract class JFrameView extends JFrame implements View, ModelListener {

    /** Serialization version (required because the superclass is serializable). */
    private static final long serialVersionUID = 1L;

    /** The model this view shows. */
    private transient Model model;

    /** The controller that handles this view's gestures. */
    private transient Controller controller;

    /**
     * Creates the view and registers it as a listener of {@code model}.
     *
     * @param model      the model to show
     * @param controller the controller that handles this view's gestures
     * @throws java.awt.HeadlessException if no display is available
     */
    public JFrameView(Model model, Controller controller) {
        setModel(model);
        setController(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Controller getController() {
        return controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model getModel() {
        return model;
    }

    /**
     * Sets the model this view shows. The view unregisters from its previous
     * model and registers with the new one.
     *
     * @param model the model to show
     */
    @Override
    public void setModel(Model model) {
        if (this.model instanceof AbstractModel old) {
            old.removeModelListener(this);
        }
        this.model = model;
        if (model instanceof AbstractModel m) {
            m.addModelListener(this);
        }
    }
}
