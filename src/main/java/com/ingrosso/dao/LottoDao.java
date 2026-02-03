package com.ingrosso.dao;

import com.ingrosso.model.Lotto;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LottoDao extends BaseDao<Lotto> {

    @Override
    protected String getTableName() {
        return "lotti";
    }

    @Override
    protected Lotto mapResultSet(ResultSet rs) throws SQLException {
        Lotto l = new Lotto();
        l.setId(rs.getInt("id"));
        l.setProdottoId(rs.getInt("prodotto_id"));
        l.setMagazzinoId(rs.getInt("magazzino_id"));
        l.setNumeroLotto(getString(rs, "numero_lotto"));
        l.setDataProduzione(getLocalDate(rs, "data_produzione"));
        l.setDataScadenza(getLocalDate(rs, "data_scadenza"));
        l.setQuantita(getBigDecimal(rs, "quantita"));
        l.setNote(getString(rs, "note"));
        l.setCreatedAt(getLocalDateTime(rs, "created_at"));
        return l;
    }

    public List<Lotto> findByProdotto(int prodottoId) {
        return executeQuery("SELECT * FROM lotti WHERE prodotto_id = ? ORDER BY data_scadenza", prodottoId);
    }

    public List<Lotto> findByProdottoAndMagazzino(int prodottoId, int magazzinoId) {
        return executeQuery(
                "SELECT * FROM lotti WHERE prodotto_id = ? AND magazzino_id = ? AND quantita > 0 ORDER BY data_scadenza",
                prodottoId, magazzinoId);
    }

    public List<Lotto> findByMagazzino(int magazzinoId) {
        return executeQuery("SELECT * FROM lotti WHERE magazzino_id = ? AND quantita > 0 ORDER BY data_scadenza", magazzinoId);
    }

    public List<Lotto> findInScadenza(int giorni) {
        LocalDate dataLimite = LocalDate.now().plusDays(giorni);
        return executeQuery(
                "SELECT * FROM lotti WHERE data_scadenza IS NOT NULL AND data_scadenza <= ? AND quantita > 0 ORDER BY data_scadenza",
                dataLimite);
    }

    public List<Lotto> findScaduti() {
        return executeQuery(
                "SELECT * FROM lotti WHERE data_scadenza IS NOT NULL AND data_scadenza < CURDATE() AND quantita > 0 ORDER BY data_scadenza");
    }

    public List<LottoCompleto> findLottiInScadenzaCompleti(int giorni) {
        String sql = """
            SELECT l.*, p.codice, p.nome as prodotto_nome, m.nome as magazzino_nome,
                   DATEDIFF(l.data_scadenza, CURDATE()) as giorni_a_scadenza
            FROM lotti l
            JOIN prodotti p ON l.prodotto_id = p.id
            JOIN magazzini m ON l.magazzino_id = m.id
            WHERE l.quantita > 0
              AND l.data_scadenza IS NOT NULL
              AND l.data_scadenza <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
            ORDER BY l.data_scadenza
            """;
        return new java.util.ArrayList<>() {{
            try (var conn = getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, giorni);
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LottoCompleto lc = new LottoCompleto();
                        lc.setId(rs.getInt("id"));
                        lc.setProdottoId(rs.getInt("prodotto_id"));
                        lc.setMagazzinoId(rs.getInt("magazzino_id"));
                        lc.setNumeroLotto(getString(rs, "numero_lotto"));
                        lc.setDataScadenza(getLocalDate(rs, "data_scadenza"));
                        lc.setQuantita(getBigDecimal(rs, "quantita"));
                        lc.setCodice(getString(rs, "codice"));
                        lc.setProdottoNome(getString(rs, "prodotto_nome"));
                        lc.setMagazzinoNome(getString(rs, "magazzino_nome"));
                        lc.setGiorniAScadenza(rs.getInt("giorni_a_scadenza"));
                        add(lc);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error finding lotti in scadenza: {}", e.getMessage());
            }
        }};
    }

    public int insert(Lotto lotto) {
        String sql = """
            INSERT INTO lotti (prodotto_id, magazzino_id, numero_lotto, data_produzione,
                data_scadenza, quantita, note)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                lotto.getProdottoId(),
                lotto.getMagazzinoId(),
                lotto.getNumeroLotto(),
                lotto.getDataProduzione(),
                lotto.getDataScadenza(),
                lotto.getQuantita(),
                lotto.getNote());
    }

    public boolean update(Lotto lotto) {
        String sql = """
            UPDATE lotti SET numero_lotto = ?, data_produzione = ?, data_scadenza = ?,
                quantita = ?, note = ?
            WHERE id = ?
            """;
        return executeUpdate(sql,
                lotto.getNumeroLotto(),
                lotto.getDataProduzione(),
                lotto.getDataScadenza(),
                lotto.getQuantita(),
                lotto.getNote(),
                lotto.getId()) > 0;
    }

    public boolean updateQuantita(int lottoId, BigDecimal quantita) {
        return executeUpdate("UPDATE lotti SET quantita = ? WHERE id = ?", quantita, lottoId) > 0;
    }

    public boolean incrementQuantita(int lottoId, BigDecimal delta) {
        return executeUpdate("UPDATE lotti SET quantita = quantita + ? WHERE id = ?", delta, lottoId) > 0;
    }

    public static class LottoCompleto {
        private int id;
        private int prodottoId;
        private int magazzinoId;
        private String numeroLotto;
        private LocalDate dataScadenza;
        private BigDecimal quantita;
        private String codice;
        private String prodottoNome;
        private String magazzinoNome;
        private int giorniAScadenza;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getProdottoId() { return prodottoId; }
        public void setProdottoId(int prodottoId) { this.prodottoId = prodottoId; }
        public int getMagazzinoId() { return magazzinoId; }
        public void setMagazzinoId(int magazzinoId) { this.magazzinoId = magazzinoId; }
        public String getNumeroLotto() { return numeroLotto; }
        public void setNumeroLotto(String numeroLotto) { this.numeroLotto = numeroLotto; }
        public LocalDate getDataScadenza() { return dataScadenza; }
        public void setDataScadenza(LocalDate dataScadenza) { this.dataScadenza = dataScadenza; }
        public BigDecimal getQuantita() { return quantita; }
        public void setQuantita(BigDecimal quantita) { this.quantita = quantita; }
        public String getCodice() { return codice; }
        public void setCodice(String codice) { this.codice = codice; }
        public String getProdottoNome() { return prodottoNome; }
        public void setProdottoNome(String prodottoNome) { this.prodottoNome = prodottoNome; }
        public String getMagazzinoNome() { return magazzinoNome; }
        public void setMagazzinoNome(String magazzinoNome) { this.magazzinoNome = magazzinoNome; }
        public int getGiorniAScadenza() { return giorniAScadenza; }
        public void setGiorniAScadenza(int giorniAScadenza) { this.giorniAScadenza = giorniAScadenza; }

        public boolean isScaduto() { return giorniAScadenza < 0; }
    }
}
