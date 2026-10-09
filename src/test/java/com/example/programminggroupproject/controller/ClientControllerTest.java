package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.MechanicShop;
import com.example.programminggroupproject.model.Service;
import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.model.Vehicle;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class ClientControllerTest {
    private final List<View> openViews = new ArrayList<>();

    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        FxTestSupport.startToolkit();
    }

    @AfterEach
    void detachViews() throws Exception {
        FxTestSupport.run(() -> openViews.forEach(view -> view.scene.setRoot(new Pane())));
    }

    @Test
    void loadingIsQueuedWithoutBlockingTheFxThreadOrShowingAZeroQuote() throws Exception {
        FakeSource source = new FakeSource();
        View view = load(source);

        FxTestSupport.run(() -> {
            assertTrue(view.executor.pending() > 0, "Initialization should queue background work");
            assertEquals(0, source.reads, "FXML loading must not perform backend reads on the FX thread");
            assertTrue(view.submit().isDisabled());
            assertEquals("Unavailable", view.label("totalPriceLabel").getText());
            assertTrue(view.radio("permissionAskRadio").isSelected());
            assertTrue(view.budget().isDisabled());
            view.notes().setText("The event thread remains responsive while loading");
        });

        drain(view);

        FxTestSupport.run(() -> {
            assertTrue(source.reads >= 3);
            assertEquals("€0.00", view.label("totalPriceLabel").getText());
            assertEquals("The event thread remains responsive while loading", view.notes().getText());
            assertEquals(1, view.vehicles().getItems().size());
            assertEquals(1, view.shops().getItems().size());
        });
    }

    @Test
    void failedCatalogShowsRetryAndCannotSubmitUntilAValidCatalogLoads() throws Exception {
        FakeSource source = new FakeSource();
        source.catalogFailure = new IllegalStateException("Catalog connection failed");
        View view = load(source);
        drain(view);

        FxTestSupport.run(() -> {
            assertTrue(view.submit().isDisabled());
            assertEquals("Unavailable", view.label("totalPriceLabel").getText());
            assertFalse(view.label("catalogMessage").getText().isBlank());
            assertTrue(view.button("retryButton").isVisible());
            assertFalse(view.button("retryButton").isDisabled());
            assertTrue(view.checks().isEmpty());
            view.submit().fire();
        });
        assertTrue(source.writes.isEmpty());

        source.catalogFailure = null;
        FxTestSupport.run(() -> view.button("retryButton").fire());
        assertTrue(view.executor.pending() > 0);
        drain(view);

        FxTestSupport.run(() -> {
            assertEquals("€0.00", view.label("totalPriceLabel").getText());
            assertEquals(1, view.checks().size());
            assertFalse(view.checks().getFirst().isDisabled());
            selectRequired(view);
            assertFalse(view.submit().isDisabled());
        });
    }

    @Test
    void emptyCatalogGivesAnActionableStateAndCannotSubmit() throws Exception {
        FakeSource source = new FakeSource();
        source.catalog = List.of();
        View view = load(source);
        drain(view);

        FxTestSupport.run(() -> {
            assertTrue(view.checks().isEmpty());
            assertTrue(view.submit().isDisabled());
            String message = view.label("catalogMessage").getText().toLowerCase(Locale.ROOT);
            assertTrue(message.contains("service") && (message.contains("no ") || message.contains("unavailable")),
                    "An empty catalog needs a useful explanation: " + message);
            assertTrue(view.button("retryButton").isVisible(), "The user should be able to reload an empty catalog");
            view.submit().fire();
        });
        assertTrue(source.writes.isEmpty());
    }

    @Test
    void unpricedAndInvalidPricesStayVisibleButCannotBeSelected() throws Exception {
        FakeSource source = new FakeSource();
        Service valid = service("Courtesy inspection", "0.00");
        source.catalog = List.of(service("Unpriced repair", null), service("Negative price", "-1.00"),
                service("Fractional cent", "2.001"), valid);
        View view = load(source);
        drain(view);

        FxTestSupport.run(() -> {
            assertEquals(4, view.checks().size());
            for (Service row : source.catalog) {
                CheckBox check = view.check(row.getId());
                assertEquals(!row.getId().equals(valid.getId()), check.isDisabled(), row.getName());
                if (check.isDisabled()) {
                    check.fire();
                    assertFalse(check.isSelected());
                }
            }
            view.check(valid.getId()).fire();
            assertTrue(view.check(valid.getId()).isSelected());
            assertEquals("€0.00", view.label("totalPriceLabel").getText());
        });
    }

    @Test
    void similarlyNamedServicesRemainSeparateAndTheDisplayedSumIsTheSavedSum() throws Exception {
        FakeSource source = new FakeSource();
        Service first = service("Oil change", "0.10");
        Service second = service("Oil filter replacement", "0.20");
        source.catalog = List.of(first, second);
        View view = load(source);
        drain(view);

        FxTestSupport.run(() -> {
            assertEquals(2, view.checks().size());
            assertNotSame(view.check(first.getId()), view.check(second.getId()));
            view.vehicles().getSelectionModel().selectFirst();
            view.shops().getSelectionModel().selectFirst();
            view.check(first.getId()).fire();
            assertEquals("€0.10", view.label("totalPriceLabel").getText());
            view.check(second.getId()).fire();
            assertEquals("€0.30", view.label("totalPriceLabel").getText());
            assertTrue(view.label("selectionCountLabel").getText().contains("2"));
            view.notes().setText("Please check tire pressure");
            view.submit().fire();
            assertTrue(view.submit().isDisabled(), "Submission must disable duplicate clicks immediately");
            view.submit().fire();
        });
        assertTrue(source.writes.isEmpty(), "Persistence must be queued, not run on the FX thread");
        assertEquals(1, view.executor.pending(), "A second click must not queue a duplicate request");
        drain(view);

        assertEquals(1, source.writes.size());
        ServiceRequest saved = source.writes.getFirst();
        assertAll(
                () -> assertEquals(source.user.getId(), saved.getClientId()),
                () -> assertEquals(source.vehicle.getId(), saved.getVehicleId()),
                () -> assertEquals(source.shop.getId(), saved.getShopId()),
                () -> assertEquals(new BigDecimal("0.30"), saved.getTotalPriceEstimated()),
                () -> assertEquals("Oil change, Oil filter replacement", saved.getServiceDescription()),
                () -> assertEquals("Pending", saved.getStatus()),
                () -> assertTrue(saved.getNotes().contains("Please check tire pressure")));
        FxTestSupport.run(() -> {
            assertTrue(view.label("messageLabel").getStyleClass().contains("status-success"));
            assertTrue(view.label("messageLabel").getText().contains("Request submitted"));
            assertTrue(view.checks().stream().noneMatch(CheckBox::isSelected));
            assertEquals("€0.00", view.label("totalPriceLabel").getText());
            assertTrue(view.radio("permissionAskRadio").isSelected());
            assertTrue(view.budget().isDisabled());
        });
    }

    @Test
    void deselectingAServiceRemovesItFromTheQuoteAndSavedRequest() throws Exception {
        FakeSource source = new FakeSource();
        Service first = service("Oil change", "39.95");
        Service second = service("Oil filter replacement", "10.05");
        source.catalog = List.of(first, second);
        View view = load(source);
        drain(view);

        FxTestSupport.run(() -> {
            selectRequired(view);
            view.check(second.getId()).fire();
            assertEquals("€50.00", view.label("totalPriceLabel").getText());
            view.check(first.getId()).fire();
            assertEquals("€10.05", view.label("totalPriceLabel").getText());
            view.submit().fire();
        });
        drain(view);
        assertEquals(1, source.writes.size());
        assertEquals(new BigDecimal("10.05"), source.writes.getFirst().getTotalPriceEstimated());
        assertEquals(second.getName(), source.writes.getFirst().getServiceDescription());
    }

    @Test
    void missingVehicleShopOrServiceNeverWritesARequest() throws Exception {
        for (String missing : List.of("vehicle", "shop", "service")) {
            FakeSource source = new FakeSource();
            View view = load(source);
            drain(view);

            FxTestSupport.run(() -> {
                selectRequired(view);
                switch (missing) {
                    case "vehicle" -> view.vehicles().getSelectionModel().clearSelection();
                    case "shop" -> view.shops().getSelectionModel().clearSelection();
                    case "service" -> view.checks().getFirst().setSelected(false);
                    default -> throw new AssertionError(missing);
                }
                view.submit().fire();
            });
            drain(view);
            assertTrue(source.writes.isEmpty(), "Missing " + missing + " must prevent persistence");
        }
    }

    @Test
    void extraWorkBudgetIsEnabledOnlyForThatPermissionAndRejectsInvalidValues() throws Exception {
        FakeSource source = new FakeSource();
        View view = load(source);
        drain(view);

        FxTestSupport.run(() -> {
            selectRequired(view);
            assertTrue(view.budget().isDisabled());
            view.radio("permissionExtraRadio").fire();
            assertFalse(view.budget().isDisabled());
        });
        for (String invalid : Arrays.asList("", "not a number", "-1", "1.001")) {
            FxTestSupport.run(() -> {
                view.budget().setText(invalid);
                view.submit().fire();
            });
            drain(view);
            assertTrue(source.writes.isEmpty(), "Invalid extra-work budget must not save: " + invalid);
            FxTestSupport.run(() -> assertFalse(view.label("messageLabel").getText().isBlank()));
        }
        FxTestSupport.run(() -> {
            view.radio("permissionStrictRadio").fire();
            assertTrue(view.budget().isDisabled());
            view.radio("permissionAskRadio").fire();
            assertTrue(view.budget().isDisabled());
        });
    }

    @Test
    void savingFailureKeepsTheSelectionAndAllowsAUserInitiatedRetry() throws Exception {
        FakeSource source = new FakeSource();
        source.saveFailure = new IllegalStateException("Cannot save request");
        View view = load(source);
        drain(view);
        FxTestSupport.run(() -> {
            selectRequired(view);
            view.notes().setText("Keep this note after an error");
            view.submit().fire();
        });
        drain(view);

        FxTestSupport.run(() -> {
            assertTrue(view.checks().getFirst().isSelected());
            assertEquals("€25.50", view.label("totalPriceLabel").getText());
            assertEquals("Keep this note after an error", view.notes().getText());
            assertFalse(view.submit().isDisabled());
            assertFalse(view.label("messageLabel").getText().isBlank());
        });
        assertEquals(1, source.writes.size());
        source.saveFailure = null;
        FxTestSupport.run(() -> view.submit().fire());
        drain(view);
        assertEquals(2, source.writes.size());
    }

    @Test
    void aSessionChangeDuringLoadingCannotExposeThePreviousClientsOptions() throws Exception {
        for (User nextUser : Arrays.asList(anotherClient(), null)) {
            FakeSource source = new FakeSource();
            View view = load(source);
            FxTestSupport.run(() -> source.user = nextUser);

            drain(view);

            assertEquals(3, source.reads, "The original load should complete before its stale result is discarded");
            assertTrue(source.writes.isEmpty());
            FxTestSupport.run(() -> {
                assertTrue(view.vehicles().getItems().isEmpty());
                assertTrue(view.shops().getItems().isEmpty());
                assertTrue(view.checks().isEmpty());
                assertEquals("Unavailable", view.label("totalPriceLabel").getText());
                assertTrue(view.submit().isDisabled());
                assertTrue(view.label("messageLabel").getStyleClass().contains("status-error"));
                assertTrue(view.label("messageLabel").getText().contains("session changed"));
                assertFalse(view.button("backButton").isDisabled());
            });
        }
    }

    @Test
    void aSessionChangeBeforeSubmissionCannotSaveAnotherClientsDraft() throws Exception {
        for (User nextUser : Arrays.asList(anotherClient(), null)) {
            FakeSource source = new FakeSource();
            View view = load(source);
            drain(view);

            FxTestSupport.run(() -> {
                selectRequired(view);
                source.user = nextUser;
                view.submit().fire();
                assertTrue(view.label("messageLabel").getStyleClass().contains("status-error"));
                assertTrue(view.label("messageLabel").getText().contains("sign in again"));
            });

            assertEquals(0, view.executor.pending(), "An invalid session must never queue a write");
            assertTrue(source.writes.isEmpty());
        }
    }

    @Test
    void aSessionChangeDuringSavingPreservesTheDraftAndRestoresNavigation() throws Exception {
        for (User nextUser : Arrays.asList(anotherClient(), null)) {
            FakeSource source = new FakeSource();
            UUID submittingClientId = source.user.getId();
            View view = load(source);
            drain(view);

            FxTestSupport.run(() -> {
                selectRequired(view);
                view.notes().setText("Keep the original client's draft");
                view.submit().fire();
                assertTrue(view.button("backButton").isDisabled());
                assertTrue(view.button("paymentsButton").isDisabled());
                source.user = nextUser;
            });
            drain(view);

            assertEquals(1, source.writes.size());
            assertEquals(submittingClientId, source.writes.getFirst().getClientId(),
                    "An already queued request must retain its original client identity");
            FxTestSupport.run(() -> {
                assertTrue(view.checks().getFirst().isSelected());
                assertEquals("Keep the original client's draft", view.notes().getText());
                assertFalse(view.node("formContent", VBox.class).isDisabled());
                assertFalse(view.button("backButton").isDisabled());
                assertFalse(view.button("paymentsButton").isDisabled());
                assertTrue(view.submit().isDisabled());
                assertTrue(view.label("messageLabel").getStyleClass().contains("status-error"));
                assertFalse(view.label("messageLabel").getStyleClass().contains("status-success"));
                assertTrue(view.label("messageLabel").getText().contains("session changed"));
            });
        }
    }

    @Test
    void aLoadCallbackCannotChangeAViewAfterItLeavesTheScene() throws Exception {
        FakeSource source = new FakeSource();
        View view = load(source);
        List<String> before = FxTestSupport.call(() -> {
            view.scene.setRoot(new Pane());
            return state(view);
        });
        drain(view);

        FxTestSupport.run(() -> assertEquals(before, state(view),
                "A completed load must not mutate a detached request screen"));
    }

    @Test
    void aSaveCallbackCannotClearOrUpdateAViewAfterItLeavesTheScene() throws Exception {
        FakeSource source = new FakeSource();
        View view = load(source);
        drain(view);
        List<String> before = FxTestSupport.call(() -> {
            selectRequired(view);
            view.notes().setText("Detached view must retain this draft");
            view.submit().fire();
            view.scene.setRoot(new Pane());
            return state(view);
        });
        drain(view);

        FxTestSupport.run(() -> assertEquals(before, state(view),
                "A completed save must not mutate a detached request screen"));
    }

    @Test
    void replacingTheStageScenePreventsAStaleLoadFromUpdatingItsOriginalRoot() throws Exception {
        View view = load(new FakeSource());
        List<String> before = FxTestSupport.call(() -> replaceStageScene(view));

        drain(view);

        FxTestSupport.run(() -> assertEquals(before, state(view),
                "Stage navigation must discard the old load even when the root still belongs to its Scene"));
    }

    @Test
    void replacingTheStageScenePreventsAStaleSaveFromClearingItsOriginalRoot() throws Exception {
        FakeSource source = new FakeSource();
        View view = load(source);
        drain(view);
        List<String> before = FxTestSupport.call(() -> {
            selectRequired(view);
            view.notes().setText("Preserve this detached draft");
            view.submit().fire();
            return replaceStageScene(view);
        });

        drain(view);

        assertEquals(1, source.writes.size());
        FxTestSupport.run(() -> assertEquals(before, state(view),
                "Stage navigation must discard the old save callback without clearing its draft"));
    }

    @Test
    void theNarrowLayoutFitsControlsAndKeepsSubmitOutsideTheScrollingForm() throws Exception {
        FakeSource source = new FakeSource();
        source.catalog = List.of(service("Oil change and filter replacement", "39.95"),
                service("Brakes and brake fluid inspection", "25.00"),
                service("Wheel alignment", "49.90"), service("Battery diagnostic", "15.00"),
                service("General inspection", "30.00"), service("Interior cleaning", "45.00"));
        View view = load(source, 600, 500);
        drain(view);

        FxTestSupport.call(() -> {
            layout(view);
            ScrollPane scroll = view.node("formScroll", ScrollPane.class);
            assertSame(scroll, view.root.getCenter());
            assertNotNull(view.root.getBottom());
            assertTrue(isDescendant(view.root.getBottom(), view.submit()));
            assertFalse(isDescendant(scroll, view.submit()));
            assertTrue(scroll.isFitToWidth());
            assertTrue(scroll.getContent().getLayoutBounds().getHeight() > scroll.getViewportBounds().getHeight(),
                    "The form must scroll at 600x500");

            List<Node> controls = new ArrayList<>(List.of(view.vehicles(), view.shops(), view.notes(), view.budget(),
                    view.radio("permissionStrictRadio"), view.radio("permissionExtraRadio"),
                    view.radio("permissionAskRadio"), view.button("backButton"), view.button("paymentsButton"),
                    view.submit()));
            controls.addAll(view.checks());
            for (Node control : controls) {
                Bounds bounds = control.localToScene(control.getLayoutBounds());
                assertTrue(bounds.getMinX() >= -1 && bounds.getMaxX() <= 601,
                        "Control must fit horizontally at 600px: " + control + " " + bounds);
            }
            Bounds footerBefore = view.submit().localToScene(view.submit().getLayoutBounds());
            assertTrue(footerBefore.getMinY() >= 0 && footerBefore.getMaxY() <= 501,
                    "Submit must be visible without scrolling");
            FxTestSupport.writeSnapshot(view.root, Path.of("target", "ui-preview", "request-600x500.png"));
            scroll.setVvalue(1);
            layout(view);
            Bounds footerAfter = view.submit().localToScene(view.submit().getLayoutBounds());
            assertEquals(footerBefore.getMinY(), footerAfter.getMinY(), 0.1,
                    "Scrolling the form must not move the submit button");
            FxTestSupport.writeSnapshot(view.root, Path.of("target", "ui-preview", "request-600x500-bottom.png"));
            return null;
        });

        View standard = load(new FakeSource());
        drain(standard);
        FxTestSupport.call(() -> {
            layout(standard);
            FxTestSupport.writeSnapshot(standard.root, Path.of("target", "ui-preview", "request-800x600.png"));
            return null;
        });

        View large = load(source, 1000, 800);
        drain(large);
        FxTestSupport.call(() -> {
            selectRequired(large);
            large.checks().get(1).setSelected(true);
            layout(large);
            FxTestSupport.writeSnapshot(large.root, Path.of("target", "ui-preview", "request-selected-1000x800.png"));
            return null;
        });

        FakeSource failingSource = new FakeSource();
        failingSource.catalogFailure = new IllegalStateException("Catalog unavailable");
        View failed = load(failingSource);
        drain(failed);
        FxTestSupport.call(() -> {
            layout(failed);
            FxTestSupport.writeSnapshot(failed.root, Path.of("target", "ui-preview", "request-error-800x600.png"));
            return null;
        });
    }

    /** Models production navigation without displaying a native window. */
    private static List<String> replaceStageScene(View view) {
        Stage stage = new Stage();
        try {
            stage.setScene(view.scene);
            assertSame(stage, view.scene.getWindow());
            stage.setScene(new Scene(new Pane(), 800, 600));
            assertSame(view.scene, view.root.getScene(), "Stage navigation must not remove the old Scene's root");
            assertNull(view.scene.getWindow(), "The abandoned Scene must lose its window");
            return state(view);
        } finally {
            stage.setScene(null);
            stage.close();
        }
    }

    private View load(FakeSource source) throws Exception {
        return load(source, 800, 600);
    }

    private View load(FakeSource source, int width, int height) throws Exception {
        View view = FxTestSupport.call(() -> {
            ManualExecutor executor = new ManualExecutor();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/programminggroupproject/client-view.fxml"));
            loader.setControllerFactory(type -> {
                assertEquals(ClientController.class, type);
                return new ClientController(source, () -> source.user, executor);
            });
            BorderPane root = loader.load();
            Scene scene = new Scene(root, width, height);
            View loaded = new View(root, scene, loader, executor);
            layout(loaded);
            return loaded;
        });
        openViews.add(view);
        return view;
    }

    private static void drain(View view) throws Exception {
        assertFalse(Platform.isFxApplicationThread(), "Background tasks must run outside the FX thread");
        int cycles = 0;
        do {
            while (view.executor.pending() > 0) {
                if (++cycles > 100) {
                    fail("Unexpected background task loop");
                }
                view.executor.runNext();
            }
            FxTestSupport.flush();
        } while (view.executor.pending() > 0);
        FxTestSupport.flush();
    }

    private static void selectRequired(View view) {
        view.vehicles().getSelectionModel().selectFirst();
        view.shops().getSelectionModel().selectFirst();
        view.checks().getFirst().setSelected(true);
    }

    private static void layout(View view) {
        view.root.resize(view.scene.getWidth(), view.scene.getHeight());
        view.root.applyCss();
        view.root.layout();
    }

    private static List<String> state(View view) {
        return List.of(view.label("catalogMessage").getText(), view.label("messageLabel").getText(),
                view.label("totalPriceLabel").getText(), view.label("selectionCountLabel").getText(),
                view.notes().getText(), Integer.toString(view.vehicles().getItems().size()),
                Integer.toString(view.shops().getItems().size()), view.checks().stream()
                        .map(check -> check.getUserData() + ":" + check.isSelected()).toList().toString(),
                Boolean.toString(view.submit().isDisabled()));
    }

    private static boolean isDescendant(Node ancestor, Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current == ancestor) {
                return true;
            }
        }
        return false;
    }

    private static Service service(String name, String price) {
        return new Service(UUID.randomUUID(), name, "Vehicle service",
                price == null ? null : new BigDecimal(price), null);
    }

    private static User anotherClient() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole("client");
        return user;
    }

    private record View(BorderPane root, Scene scene, FXMLLoader loader, ManualExecutor executor) {
        <T> T node(String id, Class<T> type) {
            Object value = loader.getNamespace().get(id);
            assertNotNull(value, "Missing FXML control: " + id);
            return type.cast(value);
        }

        Label label(String id) { return node(id, Label.class); }
        Button button(String id) { return node(id, Button.class); }
        RadioButton radio(String id) { return node(id, RadioButton.class); }
        Button submit() { return button("submitButton"); }
        TextField budget() { return node("permissionBudgetField", TextField.class); }
        TextArea notes() { return node("notesArea", TextArea.class); }

        @SuppressWarnings("unchecked")
        ComboBox<Vehicle> vehicles() { return node("vehicleComboBox", ComboBox.class); }

        @SuppressWarnings("unchecked")
        ComboBox<MechanicShop> shops() { return node("shopComboBox", ComboBox.class); }

        List<CheckBox> checks() {
            List<CheckBox> result = new ArrayList<>();
            findChecks(node("serviceList", VBox.class), result);
            return result;
        }

        CheckBox check(UUID id) {
            return checks().stream().filter(check -> id.equals(check.getUserData())).findFirst()
                    .orElseThrow(() -> new AssertionError("No service checkbox for catalog ID " + id));
        }

        private static void findChecks(Parent parent, List<CheckBox> result) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (child instanceof CheckBox check) {
                    result.add(check);
                } else if (child instanceof Parent nested) {
                    findChecks(nested, result);
                }
            }
        }
    }

    private static final class ManualExecutor implements Executor {
        private final Deque<Runnable> pending = new ArrayDeque<>();

        @Override
        public synchronized void execute(Runnable task) {
            pending.addLast(task);
        }

        synchronized int pending() {
            return pending.size();
        }

        void runNext() {
            Runnable task;
            synchronized (this) {
                task = pending.removeFirst();
            }
            task.run();
        }
    }

    private static final class FakeSource implements ClientController.DataSource {
        private User user = anotherClient();
        private final UUID initialClientId = user.getId();
        private final Vehicle vehicle;
        private final MechanicShop shop;
        private List<Service> catalog = List.of(service("Oil change", "25.50"));
        private final List<ServiceRequest> writes = new ArrayList<>();
        private RuntimeException catalogFailure;
        private RuntimeException saveFailure;
        private int reads;

        private FakeSource() {
            vehicle = new Vehicle(UUID.randomUUID(), user.getId(), "Toyota", "Corolla", 2020, "TEST-01", null);
            shop = new MechanicShop(UUID.randomUUID(), "Central Workshop", "12 Workshop Lane", "Dublin", "123", null);
        }

        @Override
        public List<Vehicle> vehicles(UUID clientId) {
            assertFalse(Platform.isFxApplicationThread(), "Vehicle loading must run outside the FX thread");
            assertEquals(initialClientId, clientId, "Loads must use the client captured when the form opened");
            reads++;
            return List.of(vehicle);
        }

        @Override
        public List<MechanicShop> shops() {
            assertFalse(Platform.isFxApplicationThread(), "Shop loading must run outside the FX thread");
            reads++;
            return List.of(shop);
        }

        @Override
        public List<Service> services() {
            assertFalse(Platform.isFxApplicationThread(), "Catalog loading must run outside the FX thread");
            reads++;
            if (catalogFailure != null) {
                throw catalogFailure;
            }
            return catalog;
        }

        @Override
        public ServiceRequest create(ServiceRequest request) {
            assertFalse(Platform.isFxApplicationThread(), "Persistence must run outside the FX thread");
            writes.add(request);
            if (saveFailure != null) {
                throw saveFailure;
            }
            ServiceRequest saved = new ServiceRequest();
            saved.setId(UUID.randomUUID());
            return saved;
        }
    }
}
