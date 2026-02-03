package com.ingrosso.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Utente {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty passwordHash = new SimpleStringProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty cognome = new SimpleStringProperty();
    private final ObjectProperty<Ruolo> ruolo = new SimpleObjectProperty<>(Ruolo.OPERATORE);
    private final BooleanProperty attivo = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDateTime> ultimoAccesso = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    public Utente() {}

    public Utente(String username, String passwordHash, String nome, String cognome, Ruolo ruolo) {
        setUsername(username);
        setPasswordHash(passwordHash);
        setNome(nome);
        setCognome(cognome);
        setRuolo(ruolo);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Username
    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }
    public StringProperty usernameProperty() { return username; }

    // Password Hash
    public String getPasswordHash() { return passwordHash.get(); }
    public void setPasswordHash(String passwordHash) { this.passwordHash.set(passwordHash); }
    public StringProperty passwordHashProperty() { return passwordHash; }

    // Nome
    public String getNome() { return nome.get(); }
    public void setNome(String nome) { this.nome.set(nome); }
    public StringProperty nomeProperty() { return nome; }

    // Cognome
    public String getCognome() { return cognome.get(); }
    public void setCognome(String cognome) { this.cognome.set(cognome); }
    public StringProperty cognomeProperty() { return cognome; }

    // Ruolo
    public Ruolo getRuolo() { return ruolo.get(); }
    public void setRuolo(Ruolo ruolo) { this.ruolo.set(ruolo); }
    public ObjectProperty<Ruolo> ruoloProperty() { return ruolo; }

    // Attivo
    public boolean isAttivo() { return attivo.get(); }
    public void setAttivo(boolean attivo) { this.attivo.set(attivo); }
    public BooleanProperty attivoProperty() { return attivo; }

    // Ultimo Accesso
    public LocalDateTime getUltimoAccesso() { return ultimoAccesso.get(); }
    public void setUltimoAccesso(LocalDateTime ultimoAccesso) { this.ultimoAccesso.set(ultimoAccesso); }
    public ObjectProperty<LocalDateTime> ultimoAccessoProperty() { return ultimoAccesso; }

    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    public String getNomeCompleto() {
        String n = getNome() != null ? getNome() : "";
        String c = getCognome() != null ? getCognome() : "";
        return (n + " " + c).trim();
    }

    @Override
    public String toString() {
        return getNomeCompleto().isEmpty() ? getUsername() : getNomeCompleto();
    }
}
