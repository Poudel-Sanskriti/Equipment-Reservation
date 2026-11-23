package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import equipmentReservation.controller.AbstractController;
import equipmentReservation.model.AbstractModel;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.ModelListener;
import equipmentReservation.view.JFrameView;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the MVC framework classes (John Hunt Calculator pattern):
 * {@link AbstractModel}, {@link ModelEvent}, {@link AbstractController} and
 * {@link JFrameView}. Covers NFR-05 (MVC structure) and NFR-07 (testability).
 */
class MvcFrameworkTest {

    /** Minimal concrete model used only by these tests. */
    private static class TestModel extends AbstractModel {
    }

    /** Minimal concrete controller used only by these tests. */
    private static class TestController extends AbstractController {
        @Override
        public void operation(String option) {
            // not needed for these tests
        }
    }

    /** Listener that records every event it receives. */
    private static class RecordingListener implements ModelListener {
        private final List<ModelEvent> received = new ArrayList<>();

        @Override
        public void modelChanged(ModelEvent e) {
            received.add(e);
        }
    }

    private static ModelEvent event(Object source) {
        return new ModelEvent(source, ActionEvent.ACTION_PERFORMED, "changed");
    }

    @Test
    void listenerReceivesTheSameEventObject() {
        TestModel model = new TestModel();
        RecordingListener listener = new RecordingListener();
        model.addModelListener(listener);

        ModelEvent e = event(model);
        model.notifyChanged(e);

        assertEquals(1, listener.received.size());
        assertSame(e, listener.received.get(0));
    }

    @Test
    void allListenersAreNotifiedInRegistrationOrder() {
        TestModel model = new TestModel();
        List<String> order = new ArrayList<>();
        model.addModelListener(e -> order.add("first"));
        model.addModelListener(e -> order.add("second"));
        model.addModelListener(e -> order.add("third"));

        model.notifyChanged(event(model));

        assertEquals(List.of("first", "second", "third"), order);
    }

    @Test
    void removedListenerIsNotNotified() {
        TestModel model = new TestModel();
        RecordingListener listener = new RecordingListener();
        model.addModelListener(listener);
        model.removeModelListener(listener);

        model.notifyChanged(event(model));

        assertTrue(listener.received.isEmpty());
    }

    @Test
    void addingSameListenerTwiceNotifiesItOnce() {
        TestModel model = new TestModel();
        RecordingListener listener = new RecordingListener();
        model.addModelListener(listener);
        model.addModelListener(listener);

        model.notifyChanged(event(model));

        assertEquals(1, listener.received.size());
    }

    @Test
    void addingNullListenerThrows() {
        TestModel model = new TestModel();
        assertThrows(NullPointerException.class, () -> model.addModelListener(null));
    }

    @Test
    void listenerCanRemoveItselfDuringNotification() {
        TestModel model = new TestModel();
        RecordingListener after = new RecordingListener();
        ModelListener selfRemoving = new ModelListener() {
            @Override
            public void modelChanged(ModelEvent e) {
                model.removeModelListener(this);
            }
        };
        model.addModelListener(selfRemoving);
        model.addModelListener(after);

        model.notifyChanged(event(model));
        model.notifyChanged(event(model));

        assertEquals(2, after.received.size());
    }

    @Test
    void modelEventKeepsSourceIdAndCommand() {
        Object source = new Object();
        ModelEvent e = new ModelEvent(source, 42, "reservationAdded");

        assertSame(source, e.getSource());
        assertEquals(42, e.getID());
        assertEquals("reservationAdded", e.getActionCommand());
    }

    @Test
    void abstractControllerStoresModelAndView() {
        TestController controller = new TestController();
        TestModel model = new TestModel();
        assertNull(controller.getModel());
        assertNull(controller.getView());

        controller.setModel(model);

        assertSame(model, controller.getModel());
    }

    @Test
    void jFrameViewRegistersItselfWithModel() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        TestModel model = new TestModel();
        TestController controller = new TestController();
        List<ModelEvent> seen = new ArrayList<>();

        JFrameView view = new JFrameView(model, controller) {
            @Override
            public void modelChanged(ModelEvent e) {
                seen.add(e);
            }
        };
        controller.setView(view);
        model.notifyChanged(event(model));

        assertEquals(1, seen.size());
        assertSame(model, view.getModel());
        assertSame(controller, view.getController());
        assertSame(view, controller.getView());
        view.dispose();
    }
}
