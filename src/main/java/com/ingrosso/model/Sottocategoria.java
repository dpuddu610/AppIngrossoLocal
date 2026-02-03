package com.ingrosso.model;

import javafx.beans.property.*;

public class Sottocategoria {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty categoriaId = new SimpleIntegerProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty descrizione = new SimpleStringProperty();
    private final IntegerProperty ordine = new SimpleIntegerProperty(0);
    private final BooleanProperty attiva = new SimpleBooleanProperty(true);
    private final ObjectProperty<Categoria> categoria = new SimpleObjectProperty<>();

    public Sottocategoria() {}

    public Sottocategoria(String nome, int categoriaId) {
        setNome(nome);
        setCategoriaId(categoriaId);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Categoria ID
    public int getCategoriaId() { return categoriaId.get(); }
    public void setCategoriaId(int categoriaId) { this.categoriaId.set(categoriaId); }
    public IntegerProperty categoriaIdProperty() { return categoriaId; }

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

    // Categoria (relazione)
    public Categoria getCategoria() { return categoria.get(); }
    public void setCategoria(Categoria categoria) { this.categoria.set(categoria); }
    public ObjectProperty<Categoria> categoriaProperty() { return categoria; }

    @Override
    public String toString() {
        return getNome();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sottocategoria that = (Sottocategoria) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
