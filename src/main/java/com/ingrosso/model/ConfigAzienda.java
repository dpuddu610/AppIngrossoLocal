package com.ingrosso.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class ConfigAzienda {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty indirizzo = new SimpleStringProperty();
    private final StringProperty citta = new SimpleStringProperty();
    private final StringProperty cap = new SimpleStringProperty();
    private final StringProperty provincia = new SimpleStringProperty();
    private final StringProperty piva = new SimpleStringProperty();
    private final StringProperty codiceFiscale = new SimpleStringProperty();
    private final StringProperty telefono = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final ObjectProperty<byte[]> logo = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    public ConfigAzienda() {}

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

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

    // Logo
    public byte[] getLogo() { return logo.get(); }
    public void setLogo(byte[] logo) { this.logo.set(logo); }
    public ObjectProperty<byte[]> logoProperty() { return logo; }

    // Updated At
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

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
}
