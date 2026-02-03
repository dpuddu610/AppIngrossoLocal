package com.ingrosso;

/**
 * Launcher class for the Gestione Ingrosso application.
 * This class is used as the main entry point for the JAR file.
 * It avoids issues with JavaFX module system when running from a shaded JAR.
 */
public class Launcher {

    public static void main(String[] args) {
        App.main(args);
    }
}
