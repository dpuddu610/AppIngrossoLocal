package com.ingrosso.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Lotto {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty prodottoId = new SimpleIntegerProperty();
    private final IntegerProperty magazzinoId = new SimpleIntegerProperty();
    private final StringProperty numeroLotto = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dataProduzione = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dataScadenza = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> quantita = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty note = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    // Relazioni
    private final ObjectProperty<Prodotto> prodotto = new SimpleObjectProperty<>();
    private final ObjectProperty<Magazzino> magazzino = new SimpleObjectProperty<>();

    public Lotto() {}

    public Lotto(int prodottoId, int magazzinoId, String numeroLotto) {
        setProdottoId(prodottoId);
        setMagazzinoId(magazzinoId);
        setNumeroLotto(numeroLotto);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Prodotto ID
    public int getProdottoId() { return prodottoId.get(); }
    public void setProdottoId(int prodottoId) { this.prodottoId.set(prodottoId); }
    public IntegerProperty prodottoIdProperty() { return prodottoId; }

    // Magazzino ID
    public int getMagazzinoId() { return magazzinoId.get(); }
    public void setMagazzinoId(int magazzinoId) { this.magazzinoId.set(magazzinoId); }
    public IntegerProperty magazzinoIdProperty() { return magazzinoId; }

    // Numero Lotto
    public String getNumeroLotto() { return numeroLotto.get(); }
    public void setNumeroLotto(String numeroLotto) { this.numeroLotto.set(numeroLotto); }
    public StringProperty numeroLottoProperty() { return numeroLotto; }

    // Data Produzione
    public LocalDate getDataProduzione() { return dataProduzione.get(); }
    public void setDataProduzione(LocalDate dataProduzione) { this.dataProduzione.set(dataProduzione); }
    public ObjectProperty<LocalDate> dataProduzioneProperty() { return dataProduzione; }

    // Data Scadenza
    public LocalDate getDataScadenza() { return dataScadenza.get(); }
    public void setDataScadenza(LocalDate dataScadenza) { this.dataScadenza.set(dataScadenza); }
    public ObjectProperty<LocalDate> dataScadenzaProperty() { return dataScadenza; }

    // Quantita
    public BigDecimal getQuantita() { return quantita.get(); }
    public void setQuantita(BigDecimal quantita) { this.quantita.set(quantita); }
    public ObjectProperty<BigDecimal> quantitaProperty() { return quantita; }

    // Note
    public String getNote() { return note.get(); }
    public void setNote(String note) { this.note.set(note); }
    public StringProperty noteProperty() { return note; }

    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // Prodotto (relazione)
    public Prodotto getProdotto() { return prodotto.get(); }
    public void setProdotto(Prodotto prodotto) { this.prodotto.set(prodotto); }
    public ObjectProperty<Prodotto> prodottoProperty() { return prodotto; }

    // Magazzino (relazione)
    public Magazzino getMagazzino() { return magazzino.get(); }
    public void setMagazzino(Magazzino magazzino) { this.magazzino.set(magazzino); }
    public ObjectProperty<Magazzino> magazzinoProperty() { return magazzino; }

    public long getGiorniAllaScadenza() {
        if (getDataScadenza() == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), getDataScadenza());
    }

    public boolean isScaduto() {
        return getGiorniAllaScadenza() < 0;
    }

    public boolean isInScadenza(int giorni) {
        long g = getGiorniAllaScadenza();
        return g >= 0 && g <= giorni;
    }

    @Override
    public String toString() {
        return getNumeroLotto();
    }
}
