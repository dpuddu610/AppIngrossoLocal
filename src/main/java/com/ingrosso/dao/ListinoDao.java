package com.ingrosso.dao;

import com.ingrosso.model.*;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ListinoDao extends BaseDao<Listino> {

    @Override
    protected String getTableName() {
        return "listini";
    }

    @Override
    protected Listino mapResultSet(ResultSet rs) throws SQLException {
        Listino l = new Listino();
        l.setId(rs.getInt("id"));
        l.setCodice(getString(rs, "codice"));
        l.setNome(getString(rs, "nome"));
        l.setDescrizione(getString(rs, "descrizione"));
        l.setTipo(TipoListino.valueOf(getString(rs, "tipo")));
        l.setDataValiditaInizio(getLocalDate(rs, "data_validita_inizio"));
        l.setDataValiditaFine(getLocalDate(rs, "data_validita_fine"));
        l.setPrincipale(getBoolean(rs, "principale"));
        l.setAttivo(getBoolean(rs, "attivo"));
        l.setCreatedAt(getLocalDateTime(rs, "created_at"));
        return l;
    }

    public List<Listino> findAllActive() {
        return executeQuery("SELECT * FROM listini WHERE attivo = TRUE ORDER BY tipo, nome");
    }

    public List<Listino> findByTipo(TipoListino tipo) {
        return executeQuery("SELECT * FROM listini WHERE tipo = ? AND attivo = TRUE ORDER BY nome", tipo.name());
    }

    public Optional<Listino> findPrincipale(TipoListino tipo) {
        return executeSingleQuery(
                "SELECT * FROM listini WHERE tipo = ? AND principale = TRUE AND attivo = TRUE LIMIT 1",
                tipo.name());
    }

    public Optional<Listino> findByCodice(String codice) {
        return executeSingleQuery("SELECT * FROM listini WHERE codice = ?", codice);
    }

    public int insert(Listino listino) {
        if (listino.isPrincipale()) {
            executeUpdate("UPDATE listini SET principale = FALSE WHERE tipo = ? AND principale = TRUE",
                    listino.getTipo().name());
        }

        String sql = """
            INSERT INTO listini (codice, nome, descrizione, tipo, data_validita_inizio,
                data_validita_fine, principale, attivo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                listino.getCodice(),
                listino.getNome(),
                listino.getDescrizione(),
                listino.getTipo().name(),
                listino.getDataValiditaInizio(),
                listino.getDataValiditaFine(),
                listino.isPrincipale(),
                listino.isAttivo());
    }

    public boolean update(Listino listino) {
        if (listino.isPrincipale()) {
            executeUpdate("UPDATE listini SET principale = FALSE WHERE tipo = ? AND principale = TRUE AND id != ?",
                    listino.getTipo().name(), listino.getId());
        }

        String sql = """
            UPDATE listini SET codice = ?, nome = ?, descrizione = ?, tipo = ?,
                data_validita_inizio = ?, data_validita_fine = ?, principale = ?, attivo = ?
            WHERE id = ?
            """;
        return executeUpdate(sql,
                listino.getCodice(),
                listino.getNome(),
                listino.getDescrizione(),
                listino.getTipo().name(),
                listino.getDataValiditaInizio(),
                listino.getDataValiditaFine(),
                listino.isPrincipale(),
                listino.isAttivo(),
                listino.getId()) > 0;
    }

    // Listino Prezzi methods
    public List<ListinoPrezzo> findPrezziByListino(int listinoId) {
        String sql = "SELECT * FROM listini_prezzi WHERE listino_id = ? ORDER BY data_inizio DESC";
        return executePrezziQuery(sql, listinoId);
    }

    public Optional<ListinoPrezzo> findPrezzoCorrente(int listinoId, int prodottoId) {
        String sql = """
            SELECT * FROM listini_prezzi
            WHERE listino_id = ? AND prodotto_id = ?
              AND data_inizio <= CURDATE()
              AND (data_fine IS NULL OR data_fine >= CURDATE())
            ORDER BY data_inizio DESC
            LIMIT 1
            """;
        List<ListinoPrezzo> list = executePrezziQuery(sql, listinoId, prodottoId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public BigDecimal getPrezzoVendita(int prodottoId) {
        Optional<Listino> listino = findPrincipale(TipoListino.VENDITA);
        if (listino.isPresent()) {
            Optional<ListinoPrezzo> prezzo = findPrezzoCorrente(listino.get().getId(), prodottoId);
            if (prezzo.isPresent()) {
                return prezzo.get().getPrezzo();
            }
        }
        return null;
    }

    public int insertPrezzo(ListinoPrezzo prezzo) {
        String sql = """
            INSERT INTO listini_prezzi (listino_id, prodotto_id, prezzo, data_inizio, data_fine)
            VALUES (?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                prezzo.getListinoId(),
                prezzo.getProdottoId(),
                prezzo.getPrezzo(),
                prezzo.getDataInizio(),
                prezzo.getDataFine());
    }

    public boolean updatePrezzo(ListinoPrezzo prezzo) {
        String sql = "UPDATE listini_prezzi SET prezzo = ?, data_inizio = ?, data_fine = ? WHERE id = ?";
        return executeUpdate(sql,
                prezzo.getPrezzo(),
                prezzo.getDataInizio(),
                prezzo.getDataFine(),
                prezzo.getId()) > 0;
    }

    public boolean deletePrezzo(int prezzoId) {
        return executeUpdate("DELETE FROM listini_prezzi WHERE id = ?", prezzoId) > 0;
    }

    private List<ListinoPrezzo> executePrezziQuery(String sql, Object... params) {
        return new java.util.ArrayList<>() {{
            try (var conn = getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ListinoPrezzo lp = new ListinoPrezzo();
                        lp.setId(rs.getInt("id"));
                        lp.setListinoId(rs.getInt("listino_id"));
                        lp.setProdottoId(rs.getInt("prodotto_id"));
                        lp.setPrezzo(getBigDecimal(rs, "prezzo"));
                        lp.setDataInizio(getLocalDate(rs, "data_inizio"));
                        lp.setDataFine(getLocalDate(rs, "data_fine"));
                        add(lp);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error executing prezzi query: {}", e.getMessage());
            }
        }};
    }

    // Storico Prezzi methods
    public List<StoricoPrezzo> findStoricoPrezzi(int prodottoId) {
        String sql = "SELECT * FROM storico_prezzi WHERE prodotto_id = ? ORDER BY data_modifica DESC";
        return executeStoricoQuery(sql, prodottoId);
    }

    public int insertStoricoPrezzo(StoricoPrezzo storico) {
        String sql = """
            INSERT INTO storico_prezzi (prodotto_id, tipo, prezzo_precedente, prezzo_nuovo,
                variazione_percentuale, utente_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                storico.getProdottoId(),
                storico.getTipo().name(),
                storico.getPrezzoPrecedente(),
                storico.getPrezzoNuovo(),
                storico.getVariazionePercentuale(),
                storico.getUtenteId() > 0 ? storico.getUtenteId() : null);
    }

    private List<StoricoPrezzo> executeStoricoQuery(String sql, Object... params) {
        return new java.util.ArrayList<>() {{
            try (var conn = getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        StoricoPrezzo sp = new StoricoPrezzo();
                        sp.setId(rs.getInt("id"));
                        sp.setProdottoId(rs.getInt("prodotto_id"));
                        sp.setTipo(TipoListino.valueOf(getString(rs, "tipo")));
                        sp.setPrezzoPrecedente(getBigDecimal(rs, "prezzo_precedente"));
                        sp.setPrezzoNuovo(getBigDecimal(rs, "prezzo_nuovo"));
                        sp.setVariazionePercentuale(getBigDecimal(rs, "variazione_percentuale"));
                        Integer utenteId = getInteger(rs, "utente_id");
                        if (utenteId != null) sp.setUtenteId(utenteId);
                        sp.setDataModifica(getLocalDateTime(rs, "data_modifica"));
                        add(sp);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error executing storico query: {}", e.getMessage());
            }
        }};
    }
}
