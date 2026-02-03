package com.ingrosso.dao;

import com.ingrosso.model.Prodotto;
import com.ingrosso.model.UnitaMisura;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProdottoDao extends BaseDao<Prodotto> {

    @Override
    protected String getTableName() {
        return "prodotti";
    }

    @Override
    protected Prodotto mapResultSet(ResultSet rs) throws SQLException {
        Prodotto p = new Prodotto();
        p.setId(rs.getInt("id"));
        p.setCodice(getString(rs, "codice"));
        p.setBarcode(getString(rs, "barcode"));
        p.setNome(getString(rs, "nome"));
        p.setDescrizione(getString(rs, "descrizione"));
        Integer sottocatId = getInteger(rs, "sottocategoria_id");
        if (sottocatId != null) p.setSottocategoriaId(sottocatId);
        p.setUnitaMisuraId(rs.getInt("unita_misura_id"));
        p.setScortaMinima(getBigDecimal(rs, "scorta_minima"));
        p.setScortaMassima(getBigDecimal(rs, "scorta_massima"));
        p.setPrezzoAcquisto(getBigDecimal(rs, "prezzo_acquisto"));
        p.setPrezzoVendita(getBigDecimal(rs, "prezzo_vendita"));
        p.setAliquotaIva(getBigDecimal(rs, "aliquota_iva"));
        p.setGestisceLotti(getBoolean(rs, "gestisce_lotti"));
        p.setNote(getString(rs, "note"));
        p.setAttivo(getBoolean(rs, "attivo"));
        p.setCreatedAt(getLocalDateTime(rs, "created_at"));
        p.setUpdatedAt(getLocalDateTime(rs, "updated_at"));
        return p;
    }

    public List<Prodotto> findAllActive() {
        return executeQuery("SELECT * FROM prodotti WHERE attivo = TRUE ORDER BY nome");
    }

    public List<Prodotto> findByCategoria(int categoriaId) {
        String sql = """
            SELECT p.* FROM prodotti p
            JOIN sottocategorie sc ON p.sottocategoria_id = sc.id
            WHERE sc.categoria_id = ? AND p.attivo = TRUE
            ORDER BY p.nome
            """;
        return executeQuery(sql, categoriaId);
    }

    public List<Prodotto> findBySottocategoria(int sottocategoriaId) {
        return executeQuery("SELECT * FROM prodotti WHERE sottocategoria_id = ? AND attivo = TRUE ORDER BY nome",
                sottocategoriaId);
    }

    public Optional<Prodotto> findByCodice(String codice) {
        return executeSingleQuery("SELECT * FROM prodotti WHERE codice = ?", codice);
    }

    public Optional<Prodotto> findByBarcode(String barcode) {
        return executeSingleQuery("SELECT * FROM prodotti WHERE barcode = ?", barcode);
    }

    public List<Prodotto> search(String text) {
        String pattern = "%" + text + "%";
        return executeQuery(
                "SELECT * FROM prodotti WHERE (codice LIKE ? OR barcode LIKE ? OR nome LIKE ?) AND attivo = TRUE ORDER BY nome",
                pattern, pattern, pattern);
    }

    public List<Prodotto> findSottoScorta(int magazzinoId) {
        String sql = """
            SELECT p.*, COALESCE(g.quantita, 0) as giacenza_attuale FROM prodotti p
            LEFT JOIN giacenze g ON p.id = g.prodotto_id AND g.magazzino_id = ?
            WHERE p.attivo = TRUE AND p.scorta_minima > 0
              AND COALESCE(g.quantita, 0) < p.scorta_minima
            ORDER BY p.nome
            """;
        return executeQuery(sql, magazzinoId);
    }

    public int insert(Prodotto prodotto) {
        String sql = """
            INSERT INTO prodotti (codice, barcode, nome, descrizione, sottocategoria_id,
                unita_misura_id, scorta_minima, scorta_massima, prezzo_acquisto, prezzo_vendita,
                aliquota_iva, gestisce_lotti, note, attivo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                prodotto.getCodice(),
                prodotto.getBarcode(),
                prodotto.getNome(),
                prodotto.getDescrizione(),
                prodotto.getSottocategoriaId() > 0 ? prodotto.getSottocategoriaId() : null,
                prodotto.getUnitaMisuraId(),
                prodotto.getScortaMinima(),
                prodotto.getScortaMassima(),
                prodotto.getPrezzoAcquisto(),
                prodotto.getPrezzoVendita(),
                prodotto.getAliquotaIva(),
                prodotto.isGestisceLotti(),
                prodotto.getNote(),
                prodotto.isAttivo());
    }

    public boolean update(Prodotto prodotto) {
        String sql = """
            UPDATE prodotti SET codice = ?, barcode = ?, nome = ?, descrizione = ?,
                sottocategoria_id = ?, unita_misura_id = ?, scorta_minima = ?, scorta_massima = ?,
                prezzo_acquisto = ?, prezzo_vendita = ?, aliquota_iva = ?, gestisce_lotti = ?,
                note = ?, attivo = ?
            WHERE id = ?
            """;
        return executeUpdate(sql,
                prodotto.getCodice(),
                prodotto.getBarcode(),
                prodotto.getNome(),
                prodotto.getDescrizione(),
                prodotto.getSottocategoriaId() > 0 ? prodotto.getSottocategoriaId() : null,
                prodotto.getUnitaMisuraId(),
                prodotto.getScortaMinima(),
                prodotto.getScortaMassima(),
                prodotto.getPrezzoAcquisto(),
                prodotto.getPrezzoVendita(),
                prodotto.getAliquotaIva(),
                prodotto.isGestisceLotti(),
                prodotto.getNote(),
                prodotto.isAttivo(),
                prodotto.getId()) > 0;
    }

    public String generateNextCode(String prefix) {
        String sql = "SELECT MAX(CAST(SUBSTRING(codice, ?) AS UNSIGNED)) FROM prodotti WHERE codice LIKE ?";
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {
            int prefixLen = prefix.length() + 1;
            stmt.setInt(1, prefixLen);
            stmt.setString(2, prefix + "%");
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int maxNum = rs.getInt(1);
                    return String.format("%s%05d", prefix, maxNum + 1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error generating code: {}", e.getMessage());
        }
        return prefix + "00001";
    }

    // Unita Misura methods
    public List<UnitaMisura> findAllUnitaMisura() {
        String sql = "SELECT * FROM unita_misura WHERE attiva = TRUE ORDER BY nome";
        return executeUnitaMisuraQuery(sql);
    }

    public UnitaMisura findUnitaMisuraById(int id) {
        String sql = "SELECT * FROM unita_misura WHERE id = ?";
        List<UnitaMisura> list = executeUnitaMisuraQuery(sql, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insertUnitaMisura(UnitaMisura um) {
        String sql = "INSERT INTO unita_misura (nome, simbolo, decimali, attiva) VALUES (?, ?, ?, ?)";
        return executeInsert(sql, um.getNome(), um.getSimbolo(), um.getDecimali(), um.isAttiva());
    }

    public boolean updateUnitaMisura(UnitaMisura um) {
        String sql = "UPDATE unita_misura SET nome = ?, simbolo = ?, decimali = ?, attiva = ? WHERE id = ?";
        return executeUpdate(sql, um.getNome(), um.getSimbolo(), um.getDecimali(), um.isAttiva(), um.getId()) > 0;
    }

    private List<UnitaMisura> executeUnitaMisuraQuery(String sql, Object... params) {
        return new java.util.ArrayList<>() {{
            try (var conn = getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        UnitaMisura um = new UnitaMisura();
                        um.setId(rs.getInt("id"));
                        um.setNome(getString(rs, "nome"));
                        um.setSimbolo(getString(rs, "simbolo"));
                        um.setDecimali(rs.getInt("decimali"));
                        um.setAttiva(getBoolean(rs, "attiva"));
                        add(um);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error executing unita misura query: {}", e.getMessage());
            }
        }};
    }
}
