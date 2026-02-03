package com.ingrosso.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Categoria {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty descrizione = new SimpleStringProperty();
    private final IntegerProperty ordine = new SimpleIntegerProperty(0);
    private final BooleanProperty attiva = new SimpleBooleanProperty(true);
    private final ObservableList<Sottocategoria> sottocategorie = FXCollections.observableArrayList();

    public Categoria() {}

    public Categoria(String nome) {
        setNome(nome);
    }

    public Categoria(String nome, String descrizione) {
        setNome(nome);
        setDescrizione(descrizione);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Nome
    public String getNome() { return nome.get(); }
    public void setNome(String nome) { this.nome.set(nome); }
    public StringProperty nomeProperty() { return nome; }

    // Descrizione
    public String getDescrizione() { return descrizione.get(); }
    public void setDescrizione(String descrizione) { this.descrizione.set(descrizione); }
    public StringProperty descrizioneProperty() { return descrizione; }

    // Ordine
    public int getOrdine() { return ordine.get(); }
    public void setOrdine(int ordine) { this.ordine.set(ordine); }
    public IntegerProperty ordineProperty() { return ordine; }

    // Attiva
    public boolean isAttiva() { return attiva.get(); }
    public void setAttiva(boolean attiva) { this.attiva.set(attiva); }
    public BooleanProperty attivaProperty() { return attiva; }

    // Sottocategorie
    public ObservableList<Sottocategoria> getSottocategorie() { return sottocategorie; }

    @Override
    public String toString() {
        return getNome();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return getId() == categoria.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
