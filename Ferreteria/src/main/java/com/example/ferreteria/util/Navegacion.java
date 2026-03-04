package com.example.ferreteria.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navegacion {

    public static void ir(Node cualquierNodoDeLaVista, String ruta, int width, int height) {
        try {
            Parent view = FXMLLoader.load(Navegacion.class.getResource(ruta));

            Stage stage = (Stage) cualquierNodoDeLaVista.getScene().getWindow();
            stage.setScene(new Scene(view, width, height));
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
