package com.ingrosso.model;

public enum Ruolo {
    ADMIN("Amministratore"),
    OPERATORE("Operatore"),
    VISUALIZZATORE("Visualizzatore");

    private final String descrizione;

    Ruolo(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }

    @Override
    public String toString() {
        return descrizione;
    }
}
