module com.ingrosso {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires java.sql;
    requires com.zaxxer.hikari;
    requires at.favre.lib.bcrypt;

    requires com.google.zxing;
    requires com.google.zxing.javase;

    requires com.github.librepdf.openpdf;
    requires org.apache.pdfbox;

    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    requires org.slf4j;

    opens com.ingrosso to javafx.fxml;
    opens com.ingrosso.controller to javafx.fxml;
    opens com.ingrosso.model to javafx.base;

    exports com.ingrosso;
    exports com.ingrosso.model;
    exports com.ingrosso.dao;
    exports com.ingrosso.service;
    exports com.ingrosso.controller;
    exports com.ingrosso.util;
}
