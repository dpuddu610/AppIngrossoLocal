package com.ingrosso.model;

public enum StatoDdt {
    BOZZA("Bozza"),
    EMESSO("Emesso"),
    ANNULLATO("Annullato");

    private final String descrizione;

    StatoDdt(String descrizione) {
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
