package com.ingrosso.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Prodotto {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty codice = new SimpleStringProperty();
    private final StringProperty barcode = new SimpleStringProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty descrizione = new SimpleStringProperty();
    private final IntegerProperty sottocategoriaId = new SimpleIntegerProperty();
    private final IntegerProperty unitaMisuraId = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> scortaMinima = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> scortaMassima = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> prezzoAcquisto = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> prezzoVendita = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> aliquotaIva = new SimpleObjectProperty<>(new BigDecimal("22.00"));
    private final BooleanProperty gestisceLotti = new SimpleBooleanProperty(false);
    private final StringProperty note = new SimpleStringProperty();
    private final BooleanProperty attivo = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Relazioni
    private final ObjectProperty<Sottocategoria> sottocategoria = new SimpleObjectProperty<>();
    private final ObjectProperty<UnitaMisura> unitaMisura = new SimpleObjectProperty<>();

    // Campo calcolato per visualizzazione giacenza totale
    private final ObjectProperty<BigDecimal> giacenzaTotale = new SimpleObjectProperty<>(BigDecimal.ZERO);

    public Prodotto() {}

    public Prodotto(String codice, String nome, int unitaMisuraId) {
        setCodice(codice);
        setNome(nome);
        setUnitaMisuraId(unitaMisuraId);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Codice
    public String getCodice() { return codice.get(); }
    public void setCodice(String codice) { this.codice.set(codice); }
    public StringProperty codiceProperty() { return codice; }

    // Barcode
    public String getBarcode() { return barcode.get(); }
    public void setBarcode(String barcode) { this.barcode.set(barcode); }
    public StringProperty barcodeProperty() { return barcode; }

    // Nome
    public String getNome() { return nome.get(); }
    public void setNome(String nome) { this.nome.set(nome); }
    public StringProperty nomeProperty() { return nome; }

    // Descrizione
    public String getDescrizione() { return descrizione.get(); }
    public void setDescrizione(String descrizione) { this.descrizione.set(descrizione); }
    public StringProperty descrizioneProperty() { return descrizione; }

    // Sottocategoria ID
    public int getSottocategoriaId() { return sottocategoriaId.get(); }
    public void setSottocategoriaId(int sottocategoriaId) { this.sottocategoriaId.set(sottocategoriaId); }
    public IntegerProperty sottocategoriaIdProperty() { return sottocategoriaId; }

    // Unita Misura ID
    public int getUnitaMisuraId() { return unitaMisuraId.get(); }
    public void setUnitaMisuraId(int unitaMisuraId) { this.unitaMisuraId.set(unitaMisuraId); }
    public IntegerProperty unitaMisuraIdProperty() { return unitaMisuraId; }

    // Scorta Minima
    public BigDecimal getScortaMinima() { return scortaMinima.get(); }
    public void setScortaMinima(BigDecimal scortaMinima) { this.scortaMinima.set(scortaMinima); }
    public ObjectProperty<BigDecimal> scortaMinimaProperty() { return scortaMinima; }

    // Scorta Massima
    public BigDecimal getScortaMassima() { return scortaMassima.get(); }
    public void setScortaMassima(BigDecimal scortaMassima) { this.scortaMassima.set(scortaMassima); }
    public ObjectProperty<BigDecimal> scortaMassimaProperty() { return scortaMassima; }

    // Prezzo Acquisto
    public BigDecimal getPrezzoAcquisto() { return prezzoAcquisto.get(); }
    public void setPrezzoAcquisto(BigDecimal prezzoAcquisto) { this.prezzoAcquisto.set(prezzoAcquisto); }
    public ObjectProperty<BigDecimal> prezzoAcquistoProperty() { return prezzoAcquisto; }

    // Prezzo Vendita
    public BigDecimal getPrezzoVendita() { return prezzoVendita.get(); }
    public void setPrezzoVendita(BigDecimal prezzoVendita) { this.prezzoVendita.set(prezzoVendita); }
    public ObjectProperty<BigDecimal> prezzoVenditaProperty() { return prezzoVendita; }

    // Aliquota IVA
    public BigDecimal getAliquotaIva() { return aliquotaIva.get(); }
    public void setAliquotaIva(BigDecimal aliquotaIva) { this.aliquotaIva.set(aliquotaIva); }
    public ObjectProperty<BigDecimal> aliquotaIvaProperty() { return aliquotaIva; }

    // Gestisce Lotti
    public boolean isGestisceLotti() { return gestisceLotti.get(); }
    public void setGestisceLotti(boolean gestisceLotti) { this.gestisceLotti.set(gestisceLotti); }
    public BooleanProperty gestisceLottiProperty() { return gestisceLotti; }

    // Note
    public String getNote() { return note.get(); }
    public void setNote(String note) { this.note.set(note); }
    public StringProperty noteProperty() { return note; }

    // Attivo
    public boolean isAttivo() { return attivo.get(); }
    public void setAttivo(boolean attivo) { this.attivo.set(attivo); }
    public BooleanProperty attivoProperty() { return attivo; }

    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // Updated At
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    // Sottocategoria (relazione)
    public Sottocategoria getSottocategoria() { return sottocategoria.get(); }
    public void setSottocategoria(Sottocategoria sottocategoria) { this.sottocategoria.set(sottocategoria); }
    public ObjectProperty<Sottocategoria> sottocategoriaProperty() { return sottocategoria; }

    // Unita Misura (relazione)
    public UnitaMisura getUnitaMisura() { return unitaMisura.get(); }
    public void setUnitaMisura(UnitaMisura unitaMisura) { this.unitaMisura.set(unitaMisura); }
    public ObjectProperty<UnitaMisura> unitaMisuraProperty() { return unitaMisura; }

    // Giacenza Totale (calcolato)
    public BigDecimal getGiacenzaTotale() { return giacenzaTotale.get(); }
    public void setGiacenzaTotale(BigDecimal giacenzaTotale) { this.giacenzaTotale.set(giacenzaTotale); }
    public ObjectProperty<BigDecimal> giacenzaTotaleProperty() { return giacenzaTotale; }

    public boolean isSottoScorta() {
        if (getScortaMinima() == null || getGiacenzaTotale() == null) return false;
        return getGiacenzaTotale().compareTo(getScortaMinima()) < 0;
    }

    @Override
    public String toString() {
        return getCodice() + " - " + getNome();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prodotto prodotto = (Prodotto) o;
        return getId() == prodotto.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
