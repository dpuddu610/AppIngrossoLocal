package com.ingrosso.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Ddt {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty numero = new SimpleIntegerProperty();
    private final IntegerProperty anno = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> dataDocumento = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> dataTrasporto = new SimpleObjectProperty<>();
    private final IntegerProperty destinatarioId = new SimpleIntegerProperty();
    private final StringProperty destinazioneDiversa = new SimpleStringProperty();
    private final IntegerProperty magazzinoId = new SimpleIntegerProperty();
    private final StringProperty causaleTrasporto = new SimpleStringProperty("Vendita");
    private final StringProperty aspettoBeni = new SimpleStringProperty();
    private final IntegerProperty colli = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> pesoKg = new SimpleObjectProperty<>();
    private final StringProperty porto = new SimpleStringProperty("Franco");
    private final StringProperty vettore = new SimpleStringProperty();
    private final StringProperty note = new SimpleStringProperty();
    private final IntegerProperty utenteId = new SimpleIntegerProperty();
    private final ObjectProperty<StatoDdt> stato = new SimpleObjectProperty<>(StatoDdt.BOZZA);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    // Relazioni
    private final ObjectProperty<Destinatario> destinatario = new SimpleObjectProperty<>();
    private final ObjectProperty<Magazzino> magazzino = new SimpleObjectProperty<>();
    private final ObjectProperty<Utente> utente = new SimpleObjectProperty<>();
    private final ObservableList<DdtRiga> righe = FXCollections.observableArrayList();

    public Ddt() {
        setAnno(LocalDate.now().getYear());
        setDataDocumento(LocalDate.now());
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Numero
    public int getNumero() { return numero.get(); }
    public void setNumero(int numero) { this.numero.set(numero); }
    public IntegerProperty numeroProperty() { return numero; }

    // Anno
    public int getAnno() { return anno.get(); }
    public void setAnno(int anno) { this.anno.set(anno); }
    public IntegerProperty annoProperty() { return anno; }

    // Data Documento
    public LocalDate getDataDocumento() { return dataDocumento.get(); }
    public void setDataDocumento(LocalDate dataDocumento) { this.dataDocumento.set(dataDocumento); }
    public ObjectProperty<LocalDate> dataDocumentoProperty() { return dataDocumento; }

    // Data Trasporto
    public LocalDateTime getDataTrasporto() { return dataTrasporto.get(); }
    public void setDataTrasporto(LocalDateTime dataTrasporto) { this.dataTrasporto.set(dataTrasporto); }
    public ObjectProperty<LocalDateTime> dataTrasportoProperty() { return dataTrasporto; }

    // Destinatario ID
    public int getDestinatarioId() { return destinatarioId.get(); }
    public void setDestinatarioId(int destinatarioId) { this.destinatarioId.set(destinatarioId); }
    public IntegerProperty destinatarioIdProperty() { return destinatarioId; }

    // Destinazione Diversa
    public String getDestinazioneDiversa() { return destinazioneDiversa.get(); }
    public void setDestinazioneDiversa(String destinazioneDiversa) { this.destinazioneDiversa.set(destinazioneDiversa); }
    public StringProperty destinazioneDiversaProperty() { return destinazioneDiversa; }

    // Magazzino ID
    public int getMagazzinoId() { return magazzinoId.get(); }
    public void setMagazzinoId(int magazzinoId) { this.magazzinoId.set(magazzinoId); }
    public IntegerProperty magazzinoIdProperty() { return magazzinoId; }

    // Causale Trasporto
    public String getCausaleTrasporto() { return causaleTrasporto.get(); }
    public void setCausaleTrasporto(String causaleTrasporto) { this.causaleTrasporto.set(causaleTrasporto); }
    public StringProperty causaleTrasportoProperty() { return causaleTrasporto; }

    // Aspetto Beni
    public String getAspettoBeni() { return aspettoBeni.get(); }
    public void setAspettoBeni(String aspettoBeni) { this.aspettoBeni.set(aspettoBeni); }
    public StringProperty aspettoBeniProperty() { return aspettoBeni; }

    // Colli
    public int getColli() { return colli.get(); }
    public void setColli(int colli) { this.colli.set(colli); }
    public IntegerProperty colliProperty() { return colli; }

    // Peso Kg
    public BigDecimal getPesoKg() { return pesoKg.get(); }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg.set(pesoKg); }
    public ObjectProperty<BigDecimal> pesoKgProperty() { return pesoKg; }

    // Porto
    public String getPorto() { return porto.get(); }
    public void setPorto(String porto) { this.porto.set(porto); }
    public StringProperty portoProperty() { return porto; }

    // Vettore
    public String getVettore() { return vettore.get(); }
    public void setVettore(String vettore) { this.vettore.set(vettore); }
    public StringProperty vettoreProperty() { return vettore; }

    // Note
    public String getNote() { return note.get(); }
    public void setNote(String note) { this.note.set(note); }
    public StringProperty noteProperty() { return note; }

    // Utente ID
    public int getUtenteId() { return utenteId.get(); }
    public void setUtenteId(int utenteId) { this.utenteId.set(utenteId); }
    public IntegerProperty utenteIdProperty() { return utenteId; }

    // Stato
    public StatoDdt getStato() { return stato.get(); }
    public void setStato(StatoDdt stato) { this.stato.set(stato); }
    public ObjectProperty<StatoDdt> statoProperty() { return stato; }

    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // Destinatario (relazione)
    public Destinatario getDestinatario() { return destinatario.get(); }
    public void setDestinatario(Destinatario destinatario) { this.destinatario.set(destinatario); }
    public ObjectProperty<Destinatario> destinatarioProperty() { return destinatario; }

    // Magazzino (relazione)
    public Magazzino getMagazzino() { return magazzino.get(); }
    public void setMagazzino(Magazzino magazzino) { this.magazzino.set(magazzino); }
    public ObjectProperty<Magazzino> magazzinoProperty() { return magazzino; }

    // Utente (relazione)
    public Utente getUtente() { return utente.get(); }
    public void setUtente(Utente utente) { this.utente.set(utente); }
    public ObjectProperty<Utente> utenteProperty() { return utente; }

    // Righe
    public ObservableList<DdtRiga> getRighe() { return righe; }

    public String getNumeroCompleto() {
        return String.format("%d/%d", getNumero(), getAnno());
    }

    public int getTotaleRighe() {
        return righe.size();
    }

    public BigDecimal getTotaleQuantita() {
        return righe.stream()
                .map(DdtRiga::getQuantita)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotaleImporto() {
        return righe.stream()
                .map(DdtRiga::getImportoRiga)
                .filter(i -> i != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return "DDT " + getNumeroCompleto();
    }
}
