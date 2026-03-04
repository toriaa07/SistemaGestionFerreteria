package com.example.ferreteria.controller;

import com.example.ferreteria.model.Producto;
import com.example.ferreteria.service.ProductoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.example.ferreteria.util.Navegacion;


public class MainController {

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colDescripcion;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Integer> colIdProveedor;

    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private TextField txtIdProveedor;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private ComboBox<String> cmbCampo;





    private final ProductoService service = new ProductoService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colIdProveedor.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));

        cargarProductos();

        cmbCampo.setItems(FXCollections.observableArrayList(
                "nombre", "descripcion", "precio", "stock", "idProveedor"
        ));
        configurarComboProductos();

        cmbProducto.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                modoAgregar();
                limpiarCampos();
            }
        });

        modoAgregar();


// Combo producto: llena los campos con el producto elegido
        cmbProducto.setOnAction(e -> {
            Producto p = cmbProducto.getSelectionModel().getSelectedItem();
            if (p == null) return;

            txtNombre.setText(p.getNombre());
            txtDescripcion.setText(p.getDescripcion());
            txtPrecio.setText(String.valueOf(p.getPrecio()));
            txtStock.setText(String.valueOf(p.getStock()));
            txtIdProveedor.setText(String.valueOf(p.getIdProveedor()));

            aplicarBloqueoSegunCampo();
        });

// Combo campo: habilita solo el campo elegido
        cmbCampo.setOnAction(e -> aplicarBloqueoSegunCampo());




    }

    private void configurarComboProductos() {
        cmbProducto.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? ""
                        : item.getNombre() + " | $" + item.getPrecio() + " | Stock: " + item.getStock());
            }
        });

        cmbProducto.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? ""
                        : item.getNombre() + " | $" + item.getPrecio() + " | Stock: " + item.getStock());
            }
        });
    }




    private void modoAgregar() {
        // habilitar todo para AGREGAR
        txtNombre.setDisable(false);
        txtDescripcion.setDisable(false);
        txtPrecio.setDisable(false);
        txtStock.setDisable(false);
        txtIdProveedor.setDisable(false);

        // opcional: limpiar combos de edición
        if (cmbProducto != null) cmbProducto.getSelectionModel().clearSelection();
        if (cmbCampo != null) cmbCampo.getSelectionModel().clearSelection();
    }

    private void modoEdicion() {
        // bloquea todo hasta que elija campo
        txtNombre.setDisable(true);
        txtDescripcion.setDisable(true);
        txtPrecio.setDisable(true);
        txtStock.setDisable(true);
        txtIdProveedor.setDisable(true);
    }

    private void aplicarBloqueoSegunCampo() {
        modoEdicion();

        String campo = cmbCampo.getSelectionModel().getSelectedItem();
        if (campo == null) return;

        switch (campo) {
            case "nombre" -> txtNombre.setDisable(false);
            case "descripcion" -> txtDescripcion.setDisable(false);
            case "precio" -> txtPrecio.setDisable(false);
            case "stock" -> txtStock.setDisable(false);
            case "idProveedor" -> txtIdProveedor.setDisable(false);
        }
    }




    @FXML
    private void cargarProductos() {
        try {
            var lista = service.getProductos();
            lista.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
            tblProductos.setItems(FXCollections.observableArrayList(lista));

            cmbProducto.setItems(FXCollections.observableArrayList(lista));
        } catch (Exception e) {
            alerta("Error", "No se pudieron cargar productos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void editarProducto() {
        Producto seleccionado = cmbProducto.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alerta("Aviso", "Selecciona un producto.", Alert.AlertType.WARNING);
            return;
        }

        String campo = cmbCampo.getSelectionModel().getSelectedItem();
        if (campo == null) {
            alerta("Aviso", "Selecciona el campo que quieres editar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Producto p = new Producto();
            p.setId(seleccionado.getId());

            switch (campo) {
                case "nombre" -> {
                    String v = txtNombre.getText().trim();
                    if (v.isEmpty()) { alerta("Validación", "Nombre obligatorio.", Alert.AlertType.WARNING); return; }
                    p.setNombre(v);
                }
                case "descripcion" -> {
                    String v = txtDescripcion.getText().trim();
                    if (v.isEmpty()) { alerta("Validación", "Descripción obligatoria.", Alert.AlertType.WARNING); return; }
                    p.setDescripcion(v);
                }
                case "precio" -> {
                    double v = Double.parseDouble(txtPrecio.getText().trim());
                    if (v < 0) { alerta("Validación", "Precio no puede ser negativo.", Alert.AlertType.WARNING); return; }
                    p.setPrecio(v);
                }
                case "stock" -> {
                    int v = Integer.parseInt(txtStock.getText().trim());
                    if (v < 0) { alerta("Validación", "Stock no puede ser negativo.", Alert.AlertType.WARNING); return; }
                    p.setStock(v);
                }
                case "idProveedor" -> {
                    int v = Integer.parseInt(txtIdProveedor.getText().trim());
                    if (v <= 0) { alerta("Validación", "IdProveedor debe ser válido.", Alert.AlertType.WARNING); return; }
                    p.setIdProveedor(v);
                }
            }

            service.patchProducto(p);

            alerta("OK", "Producto actualizado (" + campo + ").", Alert.AlertType.INFORMATION);
            cargarProductos();

        } catch (NumberFormatException nfe) {
            alerta("Error", "Valor numérico inválido en el campo seleccionado.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            alerta("Error", "No se pudo editar:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    @FXML
    private void agregarProducto() {
        try {
            String nombre = txtNombre.getText().trim();
            String descripcion = txtDescripcion.getText().trim();

            if (nombre.isEmpty() || descripcion.isEmpty()) {
                alerta("Validación", "Nombre y descripción son obligatorios.", Alert.AlertType.WARNING);
                return;
            }

            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            int idProveedor = Integer.parseInt(txtIdProveedor.getText().trim());

            if (precio < 0 || stock < 0) {
                alerta("Validación", "Precio y stock no pueden ser negativos.", Alert.AlertType.WARNING);
                return;
            }

            Producto p = new Producto(nombre, descripcion, precio, stock, idProveedor);
            service.addProducto(p);

            limpiarCampos();
            cargarProductos();
            alerta("OK", "Producto agregado correctamente.", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException nfe) {
            alerta("Error", "Precio, Stock e IdProveedor deben ser números válidos.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            alerta("Error", "No se pudo agregar:\n" + e.getMessage(), Alert.AlertType.ERROR);
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



    private void limpiarCampos() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtIdProveedor.clear();
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
