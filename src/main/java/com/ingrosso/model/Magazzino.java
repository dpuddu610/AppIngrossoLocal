package com.ingrosso.model;

import javafx.beans.property.*;

public class Magazzino {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty codice = new SimpleStringProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty indirizzo = new SimpleStringProperty();
    private final StringProperty citta = new SimpleStringProperty();
    private final BooleanProperty principale = new SimpleBooleanProperty(false);
    private final BooleanProperty attivo = new SimpleBooleanProperty(true);

    public Magazzino() {}

    public Magazzino(String codice, String nome) {
        setCodice(codice);
        setNome(nome);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Codice
    public String getCodice() { return codice.get(); }
    public void setCodice(String codice) { this.codice.set(codice); }
    public StringProperty codiceProperty() { return codice; }

    // Nome
    public String getNome() { return nome.get(); }
    public void setNome(String nome) { this.nome.set(nome); }
    public StringProperty nomeProperty() { return nome; }

    // Indirizzo
    public String getIndirizzo() { return indirizzo.get(); }
    public void setIndirizzo(String indirizzo) { this.indirizzo.set(indirizzo); }
    public StringProperty indirizzoProperty() { return indirizzo; }

    // Citta
    public String getCitta() { return citta.get(); }
    public void setCitta(String citta) { this.citta.set(citta); }
    public StringProperty cittaProperty() { return citta; }

    // Principale
    public boolean isPrincipale() { return principale.get(); }
    public void setPrincipale(boolean principale) { this.principale.set(principale); }
    public BooleanProperty principaleProperty() { return principale; }

    // Attivo
    public boolean isAttivo() { return attivo.get(); }
    public void setAttivo(boolean attivo) { this.attivo.set(attivo); }
    public BooleanProperty attivoProperty() { return attivo; }

    @Override
    public String toString() {
        return getNome();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Magazzino magazzino = (Magazzino) o;
        return getId() == magazzino.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
