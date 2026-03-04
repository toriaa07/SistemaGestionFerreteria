package com.example.ferreteria.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.ferreteria.util.Navegacion;

public class InicioController {

    @FXML private javafx.scene.layout.BorderPane root; // pon fx:id="root" en el BorderPane



    @FXML private void irProductos() {
        Navegacion.ir(root, "/com/example/ferreteria/view/productos.fxml", 1280, 720);
    }

    @FXML private void irProveedores() {
        Navegacion.ir(root, "/com/example/ferreteria/view/proveedores.fxml", 1280, 720);
    }

    @FXML private void irVentas() {
        Navegacion.ir(root, "/com/example/ferreteria/view/ventas.fxml", 1280, 720);
    }

    private void abrir(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ferreteria/view/" + fxml));
            Scene scene = new Scene(loader.load(), 1280, 720);

            Stage stage = (Stage) Stage.getWindows().filtered(w -> w.isShowing()).get(0);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }








}
