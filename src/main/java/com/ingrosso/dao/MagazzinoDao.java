package com.ingrosso.dao;

import com.ingrosso.model.Magazzino;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MagazzinoDao extends BaseDao<Magazzino> {

    @Override
    protected String getTableName() {
        return "magazzini";
    }

    @Override
    protected Magazzino mapResultSet(ResultSet rs) throws SQLException {
        Magazzino m = new Magazzino();
        m.setId(rs.getInt("id"));
        m.setCodice(getString(rs, "codice"));
        m.setNome(getString(rs, "nome"));
        m.setIndirizzo(getString(rs, "indirizzo"));
        m.setCitta(getString(rs, "citta"));
        m.setPrincipale(getBoolean(rs, "principale"));
        m.setAttivo(getBoolean(rs, "attivo"));
        return m;
    }

    public List<Magazzino> findAllActive() {
        return executeQuery("SELECT * FROM magazzini WHERE attivo = TRUE ORDER BY principale DESC, nome");
    }

    public Optional<Magazzino> findPrincipale() {
        return executeSingleQuery("SELECT * FROM magazzini WHERE principale = TRUE AND attivo = TRUE LIMIT 1");
    }

    public Optional<Magazzino> findByCodice(String codice) {
        return executeSingleQuery("SELECT * FROM magazzini WHERE codice = ?", codice);
    }

    public int insert(Magazzino magazzino) {
        // If this is set as principale, reset others
        if (magazzino.isPrincipale()) {
            executeUpdate("UPDATE magazzini SET principale = FALSE WHERE principale = TRUE");
        }

        String sql = "INSERT INTO magazzini (codice, nome, indirizzo, citta, principale, attivo) VALUES (?, ?, ?, ?, ?, ?)";
        return executeInsert(sql,
                magazzino.getCodice(),
                magazzino.getNome(),
                magazzino.getIndirizzo(),
                magazzino.getCitta(),
                magazzino.isPrincipale(),
                magazzino.isAttivo());
    }

    public boolean update(Magazzino magazzino) {
        // If this is set as principale, reset others
        if (magazzino.isPrincipale()) {
            executeUpdate("UPDATE magazzini SET principale = FALSE WHERE principale = TRUE AND id != ?", magazzino.getId());
        }

        String sql = "UPDATE magazzini SET codice = ?, nome = ?, indirizzo = ?, citta = ?, principale = ?, attivo = ? WHERE id = ?";
        return executeUpdate(sql,
                magazzino.getCodice(),
                magazzino.getNome(),
                magazzino.getIndirizzo(),
                magazzino.getCitta(),
                magazzino.isPrincipale(),
                magazzino.isAttivo(),
                magazzino.getId()) > 0;
    }

    public String generateNextCode() {
        String sql = "SELECT MAX(CAST(SUBSTRING(codice, 4) AS UNSIGNED)) FROM magazzini WHERE codice LIKE 'MAG%'";
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("MAG%02d", maxNum + 1);
            }
        } catch (SQLException e) {
            logger.error("Error generating magazzino code: {}", e.getMessage());
        }
        return "MAG01";
    }
}
