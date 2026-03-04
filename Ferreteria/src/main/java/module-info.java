module com.example.ferreteria {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    // Para que FXMLLoader pueda crear controllers y acceder a @FXML
    opens com.example.ferreteria.controller to javafx.fxml;

    opens com.example.ferreteria.model to javafx.fxml;

    // (opcional pero recomendado) si otros módulos necesitan usar tus clases
    exports com.example.ferreteria;
    exports com.example.ferreteria.controller;
    exports com.example.ferreteria.model;
}
