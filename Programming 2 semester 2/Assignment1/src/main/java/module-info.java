module assignment1 {
    requires javafx.controls;
    requires javafx.fxml;


    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.naming;

    opens assignment2.frontend.assignment1 to javafx.fxml;
    exports assignment2.frontend.assignment1;
}