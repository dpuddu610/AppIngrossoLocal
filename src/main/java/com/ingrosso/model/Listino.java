package com.ingrosso.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Listino {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty codice = new SimpleStringProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty descrizione = new SimpleStringProperty();
    private final ObjectProperty<TipoListino> tipo = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dataValiditaInizio = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dataValiditaFine = new SimpleObjectProperty<>();
    private final BooleanProperty principale = new SimpleBooleanProperty(false);
    private final BooleanProperty attivo = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    private final ObservableList<ListinoPrezzo> prezzi = FXCollections.observableArrayList();

    public Listino() {}

    public Listino(String codice, String nome, TipoListino tipo) {
        setCodice(codice);
        setNome(nome);
        setTipo(tipo);
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

    // Descrizione
    public String getDescrizione() { return descrizione.get(); }
    public void setDescrizione(String descrizione) { this.descrizione.set(descrizione); }
    public StringProperty descrizioneProperty() { return descrizione; }

    // Tipo
    public TipoListino getTipo() { return tipo.get(); }
    public void setTipo(TipoListino tipo) { this.tipo.set(tipo); }
    public ObjectProperty<TipoListino> tipoProperty() { return tipo; }

    // Data Validita Inizio
    public LocalDate getDataValiditaInizio() { return dataValiditaInizio.get(); }
    public void setDataValiditaInizio(LocalDate dataValiditaInizio) { this.dataValiditaInizio.set(dataValiditaInizio); }
    public ObjectProperty<LocalDate> dataValiditaInizioProperty() { return dataValiditaInizio; }

    // Data Validita Fine
    public LocalDate getDataValiditaFine() { return dataValiditaFine.get(); }
    public void setDataValiditaFine(LocalDate dataValiditaFine) { this.dataValiditaFine.set(dataValiditaFine); }
    public ObjectProperty<LocalDate> dataValiditaFineProperty() { return dataValiditaFine; }

    // Principale
    public boolean isPrincipale() { return principale.get(); }
    public void setPrincipale(boolean principale) { this.principale.set(principale); }
    public BooleanProperty principaleProperty() { return principale; }

    // Attivo
    public boolean isAttivo() { return attivo.get(); }
    public void setAttivo(boolean attivo) { this.attivo.set(attivo); }
    public BooleanProperty attivoProperty() { return attivo; }

    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // Prezzi
    public ObservableList<ListinoPrezzo> getPrezzi() { return prezzi; }

    public boolean isValido() {
        LocalDate oggi = LocalDate.now();
        boolean inizioOk = getDataValiditaInizio() == null || !oggi.isBefore(getDataValiditaInizio());
        boolean fineOk = getDataValiditaFine() == null || !oggi.isAfter(getDataValiditaFine());
        return inizioOk && fineOk && isAttivo();
    }

    @Override
    public String toString() {
        return getNome();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Listino listino = (Listino) o;
        return getId() == listino.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
