package com.example.ferreteria.controller;

import com.example.ferreteria.model.Producto;
import com.example.ferreteria.model.Proveedor;
import com.example.ferreteria.service.ProveedorService;
import com.example.ferreteria.util.Navegacion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ProveedorController {

    @FXML private TableView<Proveedor> tblProveedores;
    @FXML private TableColumn<Proveedor, Integer> colId;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;

    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;

    @FXML private ComboBox<Proveedor> cmbProveedores;
    @FXML private ComboBox<String> cmbCampo;

    private final ProveedorService service = new ProveedorService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        cmbCampo.setItems(FXCollections.observableArrayList(
                "nombre", "telefono", "email"
        ));

        cargarProveedores();          // llena tabla y combo
        configurarComboProveedores(); // pinta bonito el combo

        cmbProveedores.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                modoAgregar();
                limpiarCampos();
            }
        });

        modoAgregar();

        cmbProveedores.setOnAction(e -> {
            Proveedor p = cmbProveedores.getSelectionModel().getSelectedItem();
            if (p == null) return;

            txtNombre.setText(p.getNombre());
            txtTelefono.setText(p.getTelefono());
            txtEmail.setText(p.getEmail());

            aplicarBloqueoSegunCampo();
        });

        cmbCampo.setOnAction(e -> aplicarBloqueoSegunCampo());
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtTelefono.clear();
        txtEmail.clear();
    }

    private void configurarComboProveedores() {
        cmbProveedores.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? ""
                        : item.getNombre() + " | " + item.getTelefono() + " | " + item.getEmail());
            }
        });

        cmbProveedores.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });
    }

    private void modoAgregar() {
        txtNombre.setDisable(false);
        txtTelefono.setDisable(false);
        txtEmail.setDisable(false);

        if (cmbProveedores != null) cmbProveedores.getSelectionModel().clearSelection();
        if (cmbCampo != null) cmbCampo.getSelectionModel().clearSelection();
    }

    private void modoEdicion() {
        txtNombre.setDisable(true);
        txtTelefono.setDisable(true);
        txtEmail.setDisable(true);
    }

    private void aplicarBloqueoSegunCampo() {
        modoEdicion();

        String campo = cmbCampo.getSelectionModel().getSelectedItem();
        if (campo == null) return;

        switch (campo) {
            case "nombre" -> txtNombre.setDisable(false);
            case "telefono" -> txtTelefono.setDisable(false);
            case "email" -> txtEmail.setDisable(false);
        }
    }

    @FXML
    private void cargarProveedores() {
        try {
            var lista = FXCollections.observableArrayList(service.getProveedores());
            lista.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
            tblProveedores.setItems(lista);
            cmbProveedores.setItems(lista);
        } catch (Exception e) {
            alerta("Error", "No se pudieron cargar proveedores:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void agregarProveedor() {
        try {
            String nombre = txtNombre.getText().trim();
            String tel = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();

            if (nombre.isEmpty() || tel.isEmpty() || email.isEmpty()) {
                alerta("Validación", "Completa nombre, teléfono y email.", Alert.AlertType.WARNING);
                return;
            }

            service.addProveedor(new Proveedor(nombre, tel, email));
            limpiarCampos();
            cargarProveedores();
            alerta("OK", "Proveedor agregado.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            alerta("Error", "No se pudo agregar:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void editarProveedor() {
        Proveedor seleccionado = cmbProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alerta("Aviso", "Selecciona un proveedor.", Alert.AlertType.WARNING);
            return;
        }

        String campo = cmbCampo.getSelectionModel().getSelectedItem();
        if (campo == null) {
            alerta("Aviso", "Selecciona el campo que quieres editar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Proveedor p = new Proveedor();
            p.setId(seleccionado.getId());

            switch (campo) {
                case "nombre" -> {
                    String v = txtNombre.getText().trim();
                    if (v.isEmpty()) { alerta("Validación", "Nombre obligatorio.", Alert.AlertType.WARNING); return; }
                    p.setNombre(v);
                }
                case "telefono" -> {
                    String v = txtTelefono.getText().trim();
                    if (v.isEmpty()) { alerta("Validación", "Teléfono obligatorio.", Alert.AlertType.WARNING); return; }
                    p.setTelefono(v);
                }
                case "email" -> {
                    String v = txtEmail.getText().trim();
                    if (v.isEmpty()) { alerta("Validación", "Email obligatorio.", Alert.AlertType.WARNING); return; }
                    p.setEmail(v);
                }
            }

            service.patchProveedor(p);

            alerta("OK", "Proveedor actualizado (" + campo + ").", Alert.AlertType.INFORMATION);
            cargarProveedores();

        } catch (Exception e) {
            alerta("Error", "No se pudo editar:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarProveedor() {

        Proveedor seleccionado = cmbProveedores.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            alerta("Alerta", "Selecciona un proveedor para eliminar", Alert.AlertType.WARNING);
            return;
        }

        try {
            service.deleteProveedor(seleccionado.getId());
            cargarProveedores(); // método que ya usas para refrescar tabla
            alerta("OK","Proveedor eliminado correctamente.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            alerta("Error","Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML private javafx.scene.layout.BorderPane root;

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
            Scene scene = new Scene(loader.load(), 900, 550);
            Stage stage = (Stage) tblProveedores.getScene().getWindow();
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

        String borderColor = "#2563eb";
        if (tipo == Alert.AlertType.ERROR) borderColor = "#dc2626";
        if (tipo == Alert.AlertType.WARNING) borderColor = "#f59e0b";
        if (tipo == Alert.AlertType.INFORMATION) borderColor = "#16a34a";
        if (tipo == Alert.AlertType.CONFIRMATION) borderColor = "#9333ea";

        dialogPane.setStyle(
                "-fx-background-color: #0f172a;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;"
        );

        var content = dialogPane.lookup(".content.label");
        if (content != null) {
            content.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;"
            );
        }

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
