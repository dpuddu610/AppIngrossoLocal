package com.ingrosso.dao;

import com.ingrosso.model.Giacenza;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GiacenzaDao extends BaseDao<Giacenza> {

    @Override
    protected String getTableName() {
        return "giacenze";
    }

    @Override
    protected Giacenza mapResultSet(ResultSet rs) throws SQLException {
        Giacenza g = new Giacenza();
        g.setId(rs.getInt("id"));
        g.setProdottoId(rs.getInt("prodotto_id"));
        g.setMagazzinoId(rs.getInt("magazzino_id"));
        g.setQuantita(getBigDecimal(rs, "quantita"));
        g.setUpdatedAt(getLocalDateTime(rs, "updated_at"));
        return g;
    }

    public List<Giacenza> findByMagazzino(int magazzinoId) {
        return executeQuery("SELECT * FROM giacenze WHERE magazzino_id = ?", magazzinoId);
    }

    public List<Giacenza> findByProdotto(int prodottoId) {
        return executeQuery("SELECT * FROM giacenze WHERE prodotto_id = ?", prodottoId);
    }

    public Optional<Giacenza> findByProdottoAndMagazzino(int prodottoId, int magazzinoId) {
        return executeSingleQuery(
                "SELECT * FROM giacenze WHERE prodotto_id = ? AND magazzino_id = ?",
                prodottoId, magazzinoId);
    }

    public BigDecimal getGiacenzaTotale(int prodottoId) {
        String sql = "SELECT COALESCE(SUM(quantita), 0) FROM giacenze WHERE prodotto_id = ?";
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prodottoId);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting giacenza totale: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public int insert(Giacenza giacenza) {
        String sql = "INSERT INTO giacenze (prodotto_id, magazzino_id, quantita) VALUES (?, ?, ?)";
        return executeInsert(sql,
                giacenza.getProdottoId(),
                giacenza.getMagazzinoId(),
                giacenza.getQuantita());
    }

    public boolean update(Giacenza giacenza) {
        String sql = "UPDATE giacenze SET quantita = ? WHERE id = ?";
        return executeUpdate(sql, giacenza.getQuantita(), giacenza.getId()) > 0;
    }

    public boolean updateQuantita(int prodottoId, int magazzinoId, BigDecimal quantita) {
        // Try update first
        String updateSql = "UPDATE giacenze SET quantita = ? WHERE prodotto_id = ? AND magazzino_id = ?";
        int updated = executeUpdate(updateSql, quantita, prodottoId, magazzinoId);

        if (updated == 0) {
            // Insert if not exists
            String insertSql = "INSERT INTO giacenze (prodotto_id, magazzino_id, quantita) VALUES (?, ?, ?)";
            return executeInsert(insertSql, prodottoId, magazzinoId, quantita) > 0;
        }
        return true;
    }

    public boolean incrementQuantita(int prodottoId, int magazzinoId, BigDecimal delta) {
        String sql = """
            INSERT INTO giacenze (prodotto_id, magazzino_id, quantita)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE quantita = quantita + VALUES(quantita)
            """;
        return executeUpdate(sql, prodottoId, magazzinoId, delta) >= 0;
    }

    public boolean decrementQuantita(int prodottoId, int magazzinoId, BigDecimal delta) {
        return incrementQuantita(prodottoId, magazzinoId, delta.negate());
    }

    public List<GiacenzaCompleta> findGiacenzeComplete(int magazzinoId) {
        String sql = """
            SELECT g.*, p.codice, p.nome as prodotto_nome, p.scorta_minima, p.prezzo_acquisto, p.prezzo_vendita,
                   um.simbolo as unita_misura,
                   c.nome as categoria_nome, sc.nome as sottocategoria_nome
            FROM giacenze g
            JOIN prodotti p ON g.prodotto_id = p.id
            JOIN unita_misura um ON p.unita_misura_id = um.id
            LEFT JOIN sottocategorie sc ON p.sottocategoria_id = sc.id
            LEFT JOIN categorie c ON sc.categoria_id = c.id
            WHERE g.magazzino_id = ? AND p.attivo = TRUE
            ORDER BY p.nome
            """;
        return new java.util.ArrayList<>() {{
            try (var conn = getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, magazzinoId);
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        GiacenzaCompleta gc = new GiacenzaCompleta();
                        gc.setId(rs.getInt("id"));
                        gc.setProdottoId(rs.getInt("prodotto_id"));
                        gc.setMagazzinoId(rs.getInt("magazzino_id"));
                        gc.setQuantita(getBigDecimal(rs, "quantita"));
                        gc.setCodice(getString(rs, "codice"));
                        gc.setProdottoNome(getString(rs, "prodotto_nome"));
                        gc.setScortaMinima(getBigDecimal(rs, "scorta_minima"));
                        gc.setPrezzoAcquisto(getBigDecimal(rs, "prezzo_acquisto"));
                        gc.setPrezzoVendita(getBigDecimal(rs, "prezzo_vendita"));
                        gc.setUnitaMisura(getString(rs, "unita_misura"));
                        gc.setCategoria(getString(rs, "categoria_nome"));
                        gc.setSottocategoria(getString(rs, "sottocategoria_nome"));
                        add(gc);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error finding giacenze complete: {}", e.getMessage());
            }
        }};
    }

    public static class GiacenzaCompleta {
        private int id;
        private int prodottoId;
        private int magazzinoId;
        private BigDecimal quantita;
        private String codice;
        private String prodottoNome;
        private BigDecimal scortaMinima;
        private BigDecimal prezzoAcquisto;
        private BigDecimal prezzoVendita;
        private String unitaMisura;
        private String categoria;
        private String sottocategoria;

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getProdottoId() { return prodottoId; }
        public void setProdottoId(int prodottoId) { this.prodottoId = prodottoId; }
        public int getMagazzinoId() { return magazzinoId; }
        public void setMagazzinoId(int magazzinoId) { this.magazzinoId = magazzinoId; }
        public BigDecimal getQuantita() { return quantita; }
        public void setQuantita(BigDecimal quantita) { this.quantita = quantita; }
        public String getCodice() { return codice; }
        public void setCodice(String codice) { this.codice = codice; }
        public String getProdottoNome() { return prodottoNome; }
        public void setProdottoNome(String prodottoNome) { this.prodottoNome = prodottoNome; }
        public BigDecimal getScortaMinima() { return scortaMinima; }
        public void setScortaMinima(BigDecimal scortaMinima) { this.scortaMinima = scortaMinima; }
        public BigDecimal getPrezzoAcquisto() { return prezzoAcquisto; }
        public void setPrezzoAcquisto(BigDecimal prezzoAcquisto) { this.prezzoAcquisto = prezzoAcquisto; }
        public BigDecimal getPrezzoVendita() { return prezzoVendita; }
        public void setPrezzoVendita(BigDecimal prezzoVendita) { this.prezzoVendita = prezzoVendita; }
        public String getUnitaMisura() { return unitaMisura; }
        public void setUnitaMisura(String unitaMisura) { this.unitaMisura = unitaMisura; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public String getSottocategoria() { return sottocategoria; }
        public void setSottocategoria(String sottocategoria) { this.sottocategoria = sottocategoria; }

        public boolean isSottoScorta() {
            if (scortaMinima == null || quantita == null) return false;
            return quantita.compareTo(scortaMinima) < 0;
        }

        public BigDecimal getValoreAcquisto() {
            if (quantita == null || prezzoAcquisto == null) return BigDecimal.ZERO;
            return quantita.multiply(prezzoAcquisto);
        }

        public BigDecimal getValoreVendita() {
            if (quantita == null || prezzoVendita == null) return BigDecimal.ZERO;
            return quantita.multiply(prezzoVendita);
        }
    }
}
