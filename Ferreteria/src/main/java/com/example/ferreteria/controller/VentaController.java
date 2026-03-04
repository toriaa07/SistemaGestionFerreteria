package com.example.ferreteria.controller;

import com.example.ferreteria.model.Producto;
import com.example.ferreteria.model.Venta;
import com.example.ferreteria.service.ProductoService;
import com.example.ferreteria.service.VentaService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.ferreteria.util.Navegacion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.Map;


public class VentaController {

    @FXML private ComboBox<Producto> cboProductos;
    @FXML private TextField txtCantidad;
    @FXML private Label lblStock;
    @FXML private Label lblInfo;

    @FXML private TableView<Venta> tblVentas;
    @FXML private TableColumn<Venta, Integer> colVentaId;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colProductoNombre; // <- ESTA ES LA NUEVA
    @FXML private TableColumn<Venta, Integer> colCantidad;
    @FXML private TableColumn<Venta, Double> colTotal;

    private final VentaService ventaService = new VentaService();
    private final ProductoService productoService = new ProductoService();

    private final ObservableList<Venta> ventasObs = FXCollections.observableArrayList();
    private final java.util.Map<Integer, String> productoNombrePorId = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        cargarProductosCombo();

        cboProductos.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre() + " ($" + item.getPrecio() + ")");
            }
        });
        cboProductos.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre() + " ($" + item.getPrecio() + ")");
            }
        });

        cboProductos.setOnAction(e -> {
            Producto p = cboProductos.getSelectionModel().getSelectedItem();
            lblStock.setText(p == null ? "--" : String.valueOf(p.getStock()));
            lblInfo.setText("");
        });

        colVentaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadVendida"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Columna que muestra NOMBRE usando el idProducto de la venta
        colProductoNombre.setCellValueFactory(cell -> {
            int idProd = cell.getValue().getIdProducto();
            String nombre = productoNombrePorId.getOrDefault(idProd, "ID " + idProd);
            return new javafx.beans.property.SimpleStringProperty(nombre);
        });

        cargarVentas();
    }

    private void cargarVentas() {
        try {
            // 1) Cargar productos y armar el mapa id -> nombre
            productoNombrePorId.clear();
            for (Producto p : productoService.getProductos()) {
                productoNombrePorId.put(p.getId(), p.getNombre());
            }

            // 2) Cargar ventas y mostrarlas
            ventasObs.setAll(ventaService.getVentas());
            ventasObs.sort((v1, v2) -> Integer.compare(v2.getId(), v1.getId()));
            tblVentas.setItems(ventasObs);

        } catch (Exception e) {
            e.printStackTrace();
            // opcional: Alert
        }
    }


    private void cargarProductosCombo() {
        try {
            cboProductos.getItems().setAll(productoService.getProductos());
        } catch (Exception e) {
            alerta("Error", "No se pudieron cargar productos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void registrarVenta() {
        Producto p = cboProductos.getSelectionModel().getSelectedItem();
        if (p == null) {
            alerta("Aviso", "Selecciona un producto.", Alert.AlertType.WARNING);
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException ex) {
            alerta("Error", "Cantidad inválida.", Alert.AlertType.ERROR);
            return;
        }

        if (cantidad <= 0) {
            alerta("Aviso", "La cantidad debe ser mayor que 0.", Alert.AlertType.WARNING);
            return;
        }

        if (cantidad > p.getStock()) {
            alerta("Stock insuficiente", "No hay suficiente stock. Stock actual: " + p.getStock(), Alert.AlertType.WARNING);
            return;
        }

        double total = p.getPrecio() * cantidad;
        String fecha = java.time.LocalDate.now().toString();

        try {
            ventaService.registrarVenta(new Venta(fecha, p.getId(), cantidad, total));
            lblInfo.setText("Venta registrada. Total: $" + total);
            txtCantidad.clear();

            // Recargar productos para actualizar stock mostrado (si tu API descuenta stock)
            cargarProductosCombo();


// RESTAR STOCK
            productoService.restarStock(p.getId(), cantidad);

            lblInfo.setText("Venta registrada. Total: $" + total);
            txtCantidad.clear();
            cargarProductosCombo();
            cargarVentas();


        } catch (Exception e) {
            alerta("Error", "No se pudo registrar la venta:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML private javafx.scene.layout.BorderPane root; // pon fx:id="root" en el BorderPane

    @FXML private void irInicio() {
        Navegacion.ir(root, "/com/example/ferreteria/view/inicio.fxml", 1280, 720);
    }

    @FXML private void irProductos() {
        Navegacion.ir(root, "/com/example/ferreteria/view/productos.fxml", 1280, 720);
    }

    @FXML private void irProveedores() {
        Navegacion.ir(root, "/com/example/ferreteria/view/proveedores.fxml", 1280, 720);
    }

    @FXML private void irVentas() {
        Navegacion.ir(root, "/com/example/ferreteria/view/ventas.fxml", 1280, 720);
    }

    @FXML
    private void volverInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ferreteria/inicio.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 720);
            Stage stage = (Stage) cboProductos.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alerta(String titulo, String msg, Alert.AlertType tipo) {

        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane dialogPane = alert.getDialogPane();

        String borderColor = "#2563eb"; // azul default

        if (tipo == Alert.AlertType.ERROR) borderColor = "#dc2626";       // rojo
        if (tipo == Alert.AlertType.WARNING) borderColor = "#f59e0b";     // amarillo
        if (tipo == Alert.AlertType.INFORMATION) borderColor = "#16a34a"; // verde
        if (tipo == Alert.AlertType.CONFIRMATION) borderColor = "#9333ea"; // morado

        dialogPane.setStyle(
                "-fx-background-color: #0f172a;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;"
        );

        // Texto principal
        var content = dialogPane.lookup(".content.label");
        if (content != null) {
            content.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;"
            );
        }

        // Estilo de botones
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle(
                    "-fx-background-color: " + borderColor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 8 16;"
            );
        }

        // Si existe Cancel, también lo estilizamos
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setStyle(
                    "-fx-background-color: #334155;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 8 16;"
            );
        }

        alert.showAndWait();
    }
}
