package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceViewsTest {
    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        FxTestSupport.startToolkit();
    }

    @Test
    void loginLoadsWithAccessibleFieldsAndShowsBlankInputFeedbackWithoutBackendAccess() throws Exception {
        FxTestSupport.call(() -> {
            FXMLLoader loader = loader("login-view.fxml");
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 500);
            // The application also loads the legacy stylesheet; scoped workspace rules must prevail.
            scene.getStylesheets().add(getClass().getResource("/com/example/programminggroupproject/styles.css").toExternalForm());
            root.applyCss();
            root.layout();
            TextField email = (TextField) loader.getNamespace().get("usernameField");
            PasswordField password = (PasswordField) loader.getNamespace().get("passwordField");
            Label error = (Label) loader.getNamespace().get("errorLabel");
            assertFalse(error.isVisible());
            assertTrue(root.lookupAll(".label").stream().map(Label.class::cast).anyMatch(label -> label.getLabelFor() == email));
            assertTrue(root.lookupAll(".label").stream().map(Label.class::cast).anyMatch(label -> label.getLabelFor() == password));
            Button signIn = root.lookupAll(".button").stream().map(Button.class::cast).filter(Button::isDefaultButton).findFirst().orElseThrow();
            signIn.fire();
            assertFalse(error.getText().isBlank());
            assertTrue(error.isVisible());
            assertTrue(error.isManaged());
            root.applyCss();
            root.layout();
            FxTestSupport.writeSnapshot(root, Path.of("target", "ui-preview", "login-600x500.png"));
            return null;
        });
    }

    @Test
    void dashboardShowsOnlyTheCurrentRolesActionsWithoutEmptyLayoutGaps() throws Exception {
        FxTestSupport.call(() -> {
            FXMLLoader loader = loader("dashboard.fxml");
            Parent root = loader.load();
            new Scene(root, 800, 600);
            DashboardController controller = loader.getController();
            Map<String, Set<String>> expected = Map.of(
                    "client", Set.of("clientButton", "clientRequestsButton"),
                    "mechanic", Set.of("mechanicButton"),
                    "admin", Set.of("adminButton"));
            for (String role : java.util.List.of("admin", "mechanic", "client")) {
                User user = new User();
                user.setFullName("Alex Morgan");
                user.setRole(role);
                controller.setUser(user);
                for (String id : Set.of("clientButton", "clientRequestsButton", "mechanicButton", "adminButton")) {
                    Button action = (Button) loader.getNamespace().get(id);
                    assertEquals(expected.get(role).contains(id), action.isVisible());
                    assertEquals(action.isVisible(), action.isManaged());
                }
            }
            root.applyCss();
            root.layout();
            FxTestSupport.writeSnapshot(root, Path.of("target", "ui-preview", "dashboard-800x600.png"));
            return null;
        });
    }

    private static FXMLLoader loader(String file) {
        return new FXMLLoader(WorkspaceViewsTest.class.getResource("/com/example/programminggroupproject/" + file));
    }
}
