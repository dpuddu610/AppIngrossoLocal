package com.ingrosso.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Destinatario {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty codice = new SimpleStringProperty();
    private final StringProperty ragioneSociale = new SimpleStringProperty();
    private final StringProperty indirizzo = new SimpleStringProperty();
    private final StringProperty citta = new SimpleStringProperty();
    private final StringProperty cap = new SimpleStringProperty();
    private final StringProperty provincia = new SimpleStringProperty();
    private final StringProperty piva = new SimpleStringProperty();
    private final StringProperty codiceFiscale = new SimpleStringProperty();
    private final StringProperty telefono = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty note = new SimpleStringProperty();
    private final BooleanProperty attivo = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    public Destinatario() {}

    public Destinatario(String ragioneSociale) {
        setRagioneSociale(ragioneSociale);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Codice
    public String getCodice() { return codice.get(); }
    public void setCodice(String codice) { this.codice.set(codice); }
    public StringProperty codiceProperty() { return codice; }

    // Ragione Sociale
    public String getRagioneSociale() { return ragioneSociale.get(); }
    public void setRagioneSociale(String ragioneSociale) { this.ragioneSociale.set(ragioneSociale); }
    public StringProperty ragioneSocialeProperty() { return ragioneSociale; }

    // Indirizzo
    public String getIndirizzo() { return indirizzo.get(); }
    public void setIndirizzo(String indirizzo) { this.indirizzo.set(indirizzo); }
    public StringProperty indirizzoProperty() { return indirizzo; }

    // Citta
    public String getCitta() { return citta.get(); }
    public void setCitta(String citta) { this.citta.set(citta); }
    public StringProperty cittaProperty() { return citta; }

    // CAP
    public String getCap() { return cap.get(); }
    public void setCap(String cap) { this.cap.set(cap); }
    public StringProperty capProperty() { return cap; }

    // Provincia
    public String getProvincia() { return provincia.get(); }
    public void setProvincia(String provincia) { this.provincia.set(provincia); }
    public StringProperty provinciaProperty() { return provincia; }

    // P.IVA
    public String getPiva() { return piva.get(); }
    public void setPiva(String piva) { this.piva.set(piva); }
    public StringProperty pivaProperty() { return piva; }

    // Codice Fiscale
    public String getCodiceFiscale() { return codiceFiscale.get(); }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale.set(codiceFiscale); }
    public StringProperty codiceFiscaleProperty() { return codiceFiscale; }

    // Telefono
    public String getTelefono() { return telefono.get(); }
    public void setTelefono(String telefono) { this.telefono.set(telefono); }
    public StringProperty telefonoProperty() { return telefono; }

    // Email
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

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

    public String getIndirizzoCompleto() {
        StringBuilder sb = new StringBuilder();
        if (getIndirizzo() != null && !getIndirizzo().isEmpty()) {
            sb.append(getIndirizzo());
        }
        if (getCap() != null && !getCap().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(getCap());
        }
        if (getCitta() != null && !getCitta().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(getCitta());
        }
        if (getProvincia() != null && !getProvincia().isEmpty()) {
            if (sb.length() > 0) sb.append(" (").append(getProvincia()).append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getRagioneSociale();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Destinatario that = (Destinatario) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
