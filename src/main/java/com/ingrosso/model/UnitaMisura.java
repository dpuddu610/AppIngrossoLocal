package com.ingrosso.model;

import javafx.beans.property.*;

public class UnitaMisura {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty simbolo = new SimpleStringProperty();
    private final IntegerProperty decimali = new SimpleIntegerProperty(2);
    private final BooleanProperty attiva = new SimpleBooleanProperty(true);

    public UnitaMisura() {}

    public UnitaMisura(String nome, String simbolo) {
        setNome(nome);
        setSimbolo(simbolo);
    }

    public UnitaMisura(String nome, String simbolo, int decimali) {
        setNome(nome);
        setSimbolo(simbolo);
        setDecimali(decimali);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Nome
    public String getNome() { return nome.get(); }
    public void setNome(String nome) { this.nome.set(nome); }
    public StringProperty nomeProperty() { return nome; }

    // Simbolo
    public String getSimbolo() { return simbolo.get(); }
    public void setSimbolo(String simbolo) { this.simbolo.set(simbolo); }
    public StringProperty simboloProperty() { return simbolo; }

    // Decimali
    public int getDecimali() { return decimali.get(); }
    public void setDecimali(int decimali) { this.decimali.set(decimali); }
    public IntegerProperty decimaliProperty() { return decimali; }

    // Attiva
    public boolean isAttiva() { return attiva.get(); }
    public void setAttiva(boolean attiva) { this.attiva.set(attiva); }
    public BooleanProperty attivaProperty() { return attiva; }

    @Override
    public String toString() {
        return getNome() + " (" + getSimbolo() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnitaMisura that = (UnitaMisura) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
