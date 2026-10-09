package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.MechanicShop;
import com.example.programminggroupproject.model.Service;
import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.model.Vehicle;
import com.example.programminggroupproject.service.*;
import com.example.programminggroupproject.session.Session;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/** Request form: all network work runs off the JavaFX application thread. */
public class ClientController {
    @FXML private BorderPane requestRoot;
    @FXML private VBox formContent;
    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<MechanicShop> shopComboBox;
    @FXML private VBox serviceList;
    @FXML private Label catalogMessage;
    @FXML private Button retryButton;
    @FXML private RadioButton permissionStrictRadio;
    @FXML private RadioButton permissionExtraRadio;
    @FXML private RadioButton permissionAskRadio;
    @FXML private TextField permissionBudgetField;
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label selectionCountLabel;
    @FXML private Button submitButton;
    @FXML private Button backButton;
    @FXML private Button paymentsButton;

    /** Small boundary for deterministic tests and future alternative backends. */
    public interface DataSource {
        List<Vehicle> vehicles(UUID clientId);
        List<MechanicShop> shops();
        List<Service> services();
        ServiceRequest create(ServiceRequest request);
    }

    private final DataSource source;
    private final Supplier<User> currentUser;
    private final Executor executor;
    private final Map<UUID, CheckBox> choices = new LinkedHashMap<>();
    private final ToggleGroup permissionGroup = new ToggleGroup();
    private ServiceCatalog catalog;
    private UUID loadedClientId;
    private boolean loading;
    private boolean submitting;
    private boolean active = true;
    private int loadVersion;
    private final ChangeListener<Window> windowListener = (obs, previous, window) -> {
        if (previous != null && window == null) deactivate();
    };

    public ClientController() {
        this(new DataSource() {
            public List<Vehicle> vehicles(UUID clientId) {
                return VehicleService.getInstance().getByClientId(clientId);
            }
            public List<MechanicShop> shops() {
                return MechanicShopService.getInstance().getAllOrderedByName();
            }
            public List<Service> services() {
                return MechanicalService.getInstance().getAll();
            }
            public ServiceRequest create(ServiceRequest request) {
                return ServiceRequestService.getInstance().create(request);
            }
        }, Session::getCurrentUser, command -> Thread.ofVirtual().name("request-form").start(command));
    }

    ClientController(DataSource source, Supplier<User> currentUser, Executor executor) {
        this.source = source;
        this.currentUser = currentUser;
        this.executor = executor;
    }

    @FXML
    public void initialize() {
        permissionStrictRadio.setToggleGroup(permissionGroup);
        permissionExtraRadio.setToggleGroup(permissionGroup);
        permissionAskRadio.setToggleGroup(permissionGroup);
        permissionAskRadio.setSelected(true);
        permissionBudgetField.disableProperty().bind(permissionExtraRadio.selectedProperty().not());
        vehicleComboBox.setConverter(new StringConverter<>() {
            public String toString(Vehicle vehicle) {
                return vehicle == null ? "" : vehicle.getFullInfo();
            }
            public Vehicle fromString(String text) { return null; }
        });
        shopComboBox.setConverter(new StringConverter<>() {
            public String toString(MechanicShop shop) { return shop == null ? "" : shop.getName(); }
            public MechanicShop fromString(String text) { return null; }
        });
        vehicleComboBox.valueProperty().addListener((obs, oldValue, value) -> updateSummary());
        shopComboBox.valueProperty().addListener((obs, oldValue, value) -> updateSummary());
        requestRoot.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (oldScene != null) oldScene.windowProperty().removeListener(windowListener);
            if (scene != null) scene.windowProperty().addListener(windowListener);
            if (oldScene != null && scene == null) {
                deactivate();
            }
        });
        loadOptions();
    }

    @FXML
    private void handleRetry() {
        if (!loading && !submitting && active) loadOptions();
    }

    private void loadOptions() {
        User user = currentUser.get();
        if (user == null || user.getId() == null) {
            showMessage("Please sign in again before requesting a service.", "status-error");
            updateSummary();
            return;
        }
        loadedClientId = user.getId();
        UUID clientId = loadedClientId;
        int version = ++loadVersion;
        loading = true;
        catalog = null;
        choices.clear();
        serviceList.getChildren().clear();
        vehicleComboBox.getItems().clear();
        shopComboBox.getItems().clear();
        vehicleComboBox.setDisable(true);
        shopComboBox.setDisable(true);
        setCatalogMessage("Loading your vehicles, workshops, and service prices…", "status-info");
        setRetryVisible(false);
        showMessage("", "status-info");
        for (int i = 0; i < 3; i++) {
            HBox placeholder = new HBox();
            placeholder.getStyleClass().add("catalog-placeholder");
            placeholder.setMinHeight(54);
            placeholder.setAccessibleText("Loading services");
            serviceList.getChildren().add(placeholder);
        }
        updateSummary();

        CompletableFuture.supplyAsync(() -> new Options(
                List.copyOf(source.vehicles(clientId)), List.copyOf(source.shops()),
                ServiceCatalog.from(source.services())), executor)
                .whenComplete((options, failure) -> Platform.runLater(() -> {
                    if (!active || version != loadVersion) return;
                    loading = false;
                    serviceList.getChildren().clear();
                    if (!isSameUser(clientId)) {
                        showMessage("Your session changed. Please return to the dashboard and sign in again.", "status-error");
                        updateSummary();
                        return;
                    }
                    if (failure != null) {
                        setCatalogMessage("We couldn't load your booking options. Check your connection and try again.", "status-error");
                        setRetryVisible(true);
                        updateSummary();
                        return;
                    }
                    catalog = options.catalog();
                    vehicleComboBox.getItems().setAll(options.vehicles());
                    shopComboBox.getItems().setAll(options.shops());
                    vehicleComboBox.setDisable(options.vehicles().isEmpty());
                    shopComboBox.setDisable(options.shops().isEmpty());
                    vehicleComboBox.setPromptText(options.vehicles().isEmpty() ? "No vehicles registered" : "Choose your vehicle");
                    shopComboBox.setPromptText(options.shops().isEmpty() ? "No workshops available" : "Choose a workshop");
                    renderCatalog();
                    if (options.vehicles().isEmpty()) {
                        showMessage("No vehicles are registered to your account. Contact your workshop to add a vehicle, then retry.", "status-info");
                        setRetryVisible(true);
                    } else if (options.shops().isEmpty()) {
                        showMessage("No workshops are available yet. Please try again later.", "status-info");
                        setRetryVisible(true);
                    }
                    updateSummary();
                }));
    }

    private record Options(List<Vehicle> vehicles, List<MechanicShop> shops, ServiceCatalog catalog) { }

    private void renderCatalog() {
        boolean hasUnavailable = false;
        for (ServiceCatalog.Entry entry : catalog.entries()) {
            CheckBox choice = new CheckBox(entry.name());
            choice.setUserData(entry.id());
            choice.setWrapText(true);
            choice.setMaxWidth(Double.MAX_VALUE);
            choice.getStyleClass().add("service-choice");
            choice.setDisable(!entry.available());
            choices.put(entry.id(), choice);
            Label description = new Label(entry.description() == null ? "" : entry.description());
            description.setWrapText(true);
            description.getStyleClass().add("service-description");
            description.setVisible(!description.getText().isBlank());
            description.setManaged(description.isVisible());
            VBox details = new VBox(5, choice, description);
            details.setMinWidth(0);
            HBox.setHgrow(details, Priority.ALWAYS);
            Label price = new Label(entry.available() ? money(entry.price()) : "Price unavailable");
            price.getStyleClass().add("service-price");
            price.setMinWidth(Label.USE_PREF_SIZE);
            HBox row = new HBox(16, details, price);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("service-row");
            if (!entry.available()) {
                hasUnavailable = true;
                row.getStyleClass().add("unavailable");
            }
            choice.setAccessibleText(entry.name() + ", " + price.getText());
            choice.selectedProperty().addListener((obs, oldValue, selected) -> {
                row.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), selected);
                updateSummary();
            });
            serviceList.getChildren().add(row);
        }
        if (catalog.entries().isEmpty()) {
            setCatalogMessage("No services are available yet. Try again later or contact your workshop.", "status-info");
        } else if (hasUnavailable) {
            setCatalogMessage("Some services have no valid price and cannot be selected. Retry to refresh prices.", "status-info");
        } else {
            setCatalogMessage("", "status-info");
        }
        setRetryVisible(catalog.entries().isEmpty() || hasUnavailable);
    }

    private List<UUID> selectedIds() {
        return choices.entrySet().stream().filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey).toList();
    }

    private void updateSummary() {
        List<UUID> ids = selectedIds();
        boolean ready = !loading && catalog != null && catalog.entries().stream().anyMatch(ServiceCatalog.Entry::available);
        boolean valid = ready;
        if (!ready) {
            totalPriceLabel.setText("Unavailable");
        } else if (ids.isEmpty()) {
            totalPriceLabel.setText(money(BigDecimal.ZERO));
        } else {
            try {
                totalPriceLabel.setText(money(catalog.quote(ids).total()));
            } catch (IllegalArgumentException invalidSelection) {
                totalPriceLabel.setText("Unavailable");
                valid = false;
            }
        }
        selectionCountLabel.setText(ids.isEmpty() ? "No services selected" : ids.size() + (ids.size() == 1 ? " service selected" : " services selected"));
        submitButton.setDisable(!valid || ids.isEmpty() || submitting || vehicleComboBox.getValue() == null || shopComboBox.getValue() == null);
    }

    @FXML
    private void handleSubmit() {
        if (loading || submitting || !active) return;
        if (!isSameUser(loadedClientId)) {
            showMessage("Please sign in again before submitting your request.", "status-error");
            return;
        }
        Vehicle vehicle = vehicleComboBox.getValue();
        MechanicShop shop = shopComboBox.getValue();
        if (vehicle == null || shop == null) {
            showMessage("Choose a vehicle and a workshop before continuing.", "status-error");
            return;
        }
        List<UUID> ids = selectedIds();
        String notes;
        try {
            if (catalog == null) throw new IllegalArgumentException("Wait for the service prices to load, then try again.");
            catalog.quote(ids);
            notes = permissionNotes();
        } catch (IllegalArgumentException invalidRequest) {
            showMessage(invalidRequest.getMessage(), "status-error");
            return;
        }
        UUID clientId = loadedClientId;
        ServiceCatalog snapshot = catalog;
        submitting = true;
        setSubmitting(true);
        showMessage("Submitting your request…", "status-info");
        CompletableFuture.supplyAsync(() -> new ServiceRequestSubmission(source::create)
                .submit(clientId, vehicle.getId(), shop.getId(), snapshot, ids, notes), executor)
                .whenComplete((saved, failure) -> Platform.runLater(() -> {
                    if (!active) return;
                    submitting = false;
                    setSubmitting(false);
                    if (!isSameUser(clientId)) {
                        submitButton.setDisable(true);
                        showMessage("Your session changed. Return to the dashboard and sign in again to check your requests.", "status-error");
                        return;
                    }
                    if (failure != null) {
                        showMessage("We couldn't confirm your request. Check My requests from the dashboard before submitting again.", "status-error");
                        return;
                    }
                    choices.values().forEach(choice -> choice.setSelected(false));
                    permissionAskRadio.setSelected(true);
                    permissionBudgetField.clear();
                    notesArea.clear();
                    updateSummary();
                    showMessage("Request submitted. Follow its progress in My requests from the dashboard.", "status-success");
                }));
    }

    private String permissionNotes() {
        String permission;
        if (permissionStrictRadio.isSelected()) {
            permission = "Only the selected services, nothing else.";
        } else if (permissionExtraRadio.isSelected()) {
            String input = permissionBudgetField.getText().trim().replace(',', '.');
            if (!input.matches("[0-9]+(\\.[0-9]{1,2})?")) {
                permissionBudgetField.requestFocus();
                throw new IllegalArgumentException("Enter an extra-work limit of zero or more, with up to two decimal places.");
            }
            BigDecimal budget = new BigDecimal(input);
            permission = "Mechanic may do extra work up to " + money(budget) + ".";
        } else if (permissionAskRadio.isSelected()) {
            permission = "Mechanic must ask before doing anything else.";
        } else {
            throw new IllegalArgumentException("Choose how the mechanic should handle additional work.");
        }
        String notes = notesArea.getText().trim();
        return "PERMISSIONS: " + permission + (notes.isEmpty() ? "" : "\n\nCLIENT NOTES: " + notes);
    }

    private void setSubmitting(boolean busy) {
        formContent.setDisable(busy);
        backButton.setDisable(busy);
        paymentsButton.setDisable(busy);
        submitButton.setText(busy ? "Submitting…" : "Submit request");
        updateSummary();
    }

    private boolean isSameUser(UUID clientId) {
        User user = currentUser.get();
        return clientId != null && user != null && clientId.equals(user.getId());
    }

    private void deactivate() {
        active = false;
        loadVersion++;
    }

    private static String money(BigDecimal amount) { return "€" + amount.setScale(2).toPlainString(); }

    private void setCatalogMessage(String text, String style) { setStatus(catalogMessage, text, style); }
    private void showMessage(String text, String style) { setStatus(messageLabel, text, style); }
    private static void setStatus(Label label, String text, String style) {
        label.getStyleClass().removeAll("status-error", "status-info", "status-success");
        label.getStyleClass().add(style);
        label.setText(text);
        label.setVisible(!text.isBlank());
        label.setManaged(!text.isBlank());
    }
    private void setRetryVisible(boolean visible) {
        retryButton.setVisible(visible);
        retryButton.setManaged(visible);
    }

    @FXML private void handleBack() { navigate("dashboard.fxml", "Dashboard - Car Servicinator 3000"); }
    @FXML private void handleViewPayments() { navigate("payment-view.fxml", "Payments - Car Servicinator 3000"); }

    private void navigate(String view, String title) {
        if (submitting) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/programminggroupproject/" + view));
            Scene scene = new Scene(loader.load(), requestRoot.getScene().getWidth(), requestRoot.getScene().getHeight());
            if (loader.getController() instanceof DashboardController dashboard) dashboard.setUser(currentUser.get());
            Stage stage = (Stage) requestRoot.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException failure) {
            showMessage("We couldn't open that screen. Please try again.", "status-error");
        }
    }
}
