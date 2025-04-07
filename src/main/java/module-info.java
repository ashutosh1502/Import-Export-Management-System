module com.project.application {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires java.naming;
    requires java.desktop;
    requires java.sql;
    requires com.oracle.database.jdbc;
    requires itextpdf;
//    requires org.slf4j;
//    requires ch.qos.logback.classic;

    exports com.project.application;
    exports com.project.utils;
}