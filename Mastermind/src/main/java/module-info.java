module org.example.mastermind {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    opens org.example.mastermind to javafx.fxml;
    exports org.example.mastermind;
}