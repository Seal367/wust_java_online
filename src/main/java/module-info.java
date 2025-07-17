module com.example.javafx3 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.javafx3 to javafx.fxml;
    opens com.example.javafx3.ui to javafx.fxml;
    exports com.example.javafx3;

    exports com.example.javafx3.manager;
    exports com.example.javafx3.model;
//    exports com.example.javafx3.ui;
}