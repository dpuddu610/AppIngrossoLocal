package com.ingrosso.model;

public enum TipoListino {
    ACQUISTO("Acquisto"),
    VENDITA("Vendita");

    private final String descrizione;

    TipoListino(String descrizione) {
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
