package com.ingrosso.model;

public enum TipoMovimento {
    CARICO("Carico"),
    SCARICO("Scarico"),
    RETTIFICA("Rettifica"),
    TRASFERIMENTO("Trasferimento");

    private final String descrizione;

    TipoMovimento(String descrizione) {
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
