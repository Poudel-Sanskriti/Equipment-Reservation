package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import equipmentReservation.controller.AbstractController;
import equipmentReservation.model.AbstractModel;
import equipmentReservation.model.ModelEvent;
import equipmentReservation.model.ModelListener;
import equipmentReservation.view.JFrameView;

/**
 * Tests for the MVC framework classes adapted from the John Hunt calculator example
 * (increment 03; supports NFR-05).
 */
public class MvcFrameworkTest {

    /** Minimal concrete model used to exercise {@link AbstractModel}. */
    private static class TestModel extends AbstractModel {
    }

    /** Minimal concrete controller used to exercise {@link AbstractController}. */
    private static class TestController extends AbstractController {
        private final List<String> commands = new ArrayList<>();

        @Override
        public void operation(String command) {
            commands.add(command);
        }
    }

    /** Minimal concrete view that records the thread {@code refresh} runs on. */
    private static class TestView extends JFrameView {
        private static final long serialVersionUID = 1L;
        private final CountDownLatch refreshed = new CountDownLatch(1);
        private final AtomicBoolean refreshedOnEdt = new AtomicBoolean(false);

        TestView(TestModel model, TestController controller) {
            super(model, controller);
        }

        @Override
        protected void refresh(ModelEvent event) {
            refreshedOnEdt.set(SwingUtilities.isEventDispatchThread());
            refreshed.countDown();
        }
    }

    @Test
    public void listenerReceivesEventUntilRemoved() {
        TestModel model = new TestModel();
        List<ModelEvent> received = new ArrayList<>();
        ModelListener listener = received::add;

        model.addModelListener(listener);
        ModelEvent first = new ModelEvent(model, ModelEvent.RESERVATIONS);
        model.notifyChanged(first);
        model.removeModelListener(listener);
        model.notifyChanged(new ModelEvent(model, ModelEvent.EQUIPMENT));

        assertEquals(1, received.size());
        assertSame(first, received.get(0));
    }

    @Test
    public void listenerMayRemoveItselfDuringNotification() {
        TestModel model = new TestModel();
        List<String> calls = new ArrayList<>();
        ModelListener selfRemoving = new ModelListener() {
            @Override
            public void modelChanged(ModelEvent event) {
                calls.add("self");
                model.removeModelListener(this);
            }
        };
        model.addModelListener(selfRemoving);
        model.addModelListener(event -> calls.add("other"));

        model.notifyChanged(new ModelEvent(model, ModelEvent.SESSION));
        model.notifyChanged(new ModelEvent(model, ModelEvent.SESSION));

        assertEquals(List.of("self", "other", "other"), calls);
    }

    @Test
    public void modelEventCarriesSourceAndWhat() {
        Object source = new Object();
        ModelEvent event = new ModelEvent(source, ModelEvent.MAINTENANCE);

        assertSame(source, event.getSource());
        assertEquals(ModelEvent.MAINTENANCE, event.getActionCommand());
    }

    @Test
    public void controllerStoresModelAndDispatchesOperations() {
        TestController controller = new TestController();
        TestModel model = new TestModel();

        assertNull(controller.getModel());
        controller.setModel(model);
        controller.operation("SEARCH");

        assertSame(model, controller.getModel());
        assertEquals(List.of("SEARCH"), controller.commands);
    }

    @Test
    public void viewRefreshesOnEdtWhenEventFiredFromBackgroundThread() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        TestModel model = new TestModel();
        TestController controller = new TestController();
        TestView[] holder = new TestView[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TestView(model, controller));
        TestView view = holder[0];

        Thread background = new Thread(() -> model.notifyChanged(new ModelEvent(model, ModelEvent.RESERVATIONS)));
        background.start();
        background.join();

        assertTrue(view.refreshed.await(5, TimeUnit.SECONDS), "refresh was not called");
        assertTrue(view.refreshedOnEdt.get(), "refresh must run on the EDT");
        assertSame(model, view.getModel());
        assertSame(controller, view.getController());
        SwingUtilities.invokeAndWait(view::dispose);
    }

    @Test
    public void disposedViewStopsListening() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        TestModel model = new TestModel();
        TestView[] holder = new TestView[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TestView(model, new TestController()));
        SwingUtilities.invokeAndWait(holder[0]::dispose);

        model.notifyChanged(new ModelEvent(model, ModelEvent.RESERVATIONS));
        SwingUtilities.invokeAndWait(() -> { });

        assertEquals(1, holder[0].refreshed.getCount(), "disposed view must not refresh");
    }
}
