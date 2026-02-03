package com.ingrosso.dao;

import com.ingrosso.model.ConfigAzienda;
import com.ingrosso.model.Ruolo;
import com.ingrosso.model.Utente;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UtenteDao extends BaseDao<Utente> {

    @Override
    protected String getTableName() {
        return "utenti";
    }

    @Override
    protected Utente mapResultSet(ResultSet rs) throws SQLException {
        Utente u = new Utente();
        u.setId(rs.getInt("id"));
        u.setUsername(getString(rs, "username"));
        u.setPasswordHash(getString(rs, "password_hash"));
        u.setNome(getString(rs, "nome"));
        u.setCognome(getString(rs, "cognome"));
        u.setRuolo(Ruolo.valueOf(getString(rs, "ruolo")));
        u.setAttivo(getBoolean(rs, "attivo"));
        u.setUltimoAccesso(getLocalDateTime(rs, "ultimo_accesso"));
        u.setCreatedAt(getLocalDateTime(rs, "created_at"));
        return u;
    }

    public List<Utente> findAllActive() {
        return executeQuery("SELECT * FROM utenti WHERE attivo = TRUE ORDER BY cognome, nome");
    }

    public Optional<Utente> findByUsername(String username) {
        return executeSingleQuery("SELECT * FROM utenti WHERE username = ?", username);
    }

    public List<Utente> findByRuolo(Ruolo ruolo) {
        return executeQuery("SELECT * FROM utenti WHERE ruolo = ? AND attivo = TRUE ORDER BY cognome, nome",
                ruolo.name());
    }

    public int insert(Utente utente) {
        String sql = """
            INSERT INTO utenti (username, password_hash, nome, cognome, ruolo, attivo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                utente.getUsername(),
                utente.getPasswordHash(),
                utente.getNome(),
                utente.getCognome(),
                utente.getRuolo().name(),
                utente.isAttivo());
    }

    public boolean update(Utente utente) {
        String sql = """
            UPDATE utenti SET username = ?, nome = ?, cognome = ?, ruolo = ?, attivo = ?
            WHERE id = ?
            """;
        return executeUpdate(sql,
                utente.getUsername(),
                utente.getNome(),
                utente.getCognome(),
                utente.getRuolo().name(),
                utente.isAttivo(),
                utente.getId()) > 0;
    }

    public boolean updatePassword(int utenteId, String passwordHash) {
        return executeUpdate("UPDATE utenti SET password_hash = ? WHERE id = ?", passwordHash, utenteId) > 0;
    }

    public boolean updateUltimoAccesso(int utenteId) {
        return executeUpdate("UPDATE utenti SET ultimo_accesso = ? WHERE id = ?",
                LocalDateTime.now(), utenteId) > 0;
    }

    // Config Azienda methods
    public Optional<ConfigAzienda> getConfigAzienda() {
        String sql = "SELECT * FROM config_azienda WHERE id = 1";
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {
            if (rs.next()) {
                ConfigAzienda config = new ConfigAzienda();
                config.setId(rs.getInt("id"));
                config.setNome(getString(rs, "nome"));
                config.setIndirizzo(getString(rs, "indirizzo"));
                config.setCitta(getString(rs, "citta"));
                config.setCap(getString(rs, "cap"));
                config.setProvincia(getString(rs, "provincia"));
                config.setPiva(getString(rs, "piva"));
                config.setCodiceFiscale(getString(rs, "codice_fiscale"));
                config.setTelefono(getString(rs, "telefono"));
                config.setEmail(getString(rs, "email"));
                config.setLogo(getBytes(rs, "logo"));
                config.setUpdatedAt(getLocalDateTime(rs, "updated_at"));
                return Optional.of(config);
            }
        } catch (SQLException e) {
            logger.error("Error getting config azienda: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public boolean saveConfigAzienda(ConfigAzienda config) {
        String sql = """
            INSERT INTO config_azienda (id, nome, indirizzo, citta, cap, provincia, piva,
                codice_fiscale, telefono, email, logo)
            VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                nome = VALUES(nome), indirizzo = VALUES(indirizzo), citta = VALUES(citta),
                cap = VALUES(cap), provincia = VALUES(provincia), piva = VALUES(piva),
                codice_fiscale = VALUES(codice_fiscale), telefono = VALUES(telefono),
                email = VALUES(email), logo = VALUES(logo)
            """;
        return executeUpdate(sql,
                config.getNome(),
                config.getIndirizzo(),
                config.getCitta(),
                config.getCap(),
                config.getProvincia(),
                config.getPiva(),
                config.getCodiceFiscale(),
                config.getTelefono(),
                config.getEmail(),
                config.getLogo()) >= 0;
    }
}
