package com.ingrosso.dao;

import com.ingrosso.model.Movimento;
import com.ingrosso.model.TipoMovimento;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MovimentoDao extends BaseDao<Movimento> {

    @Override
    protected String getTableName() {
        return "movimenti";
    }

    @Override
    protected Movimento mapResultSet(ResultSet rs) throws SQLException {
        Movimento m = new Movimento();
        m.setId(rs.getInt("id"));
        m.setProdottoId(rs.getInt("prodotto_id"));
        m.setMagazzinoId(rs.getInt("magazzino_id"));
        Integer lottoId = getInteger(rs, "lotto_id");
        if (lottoId != null) m.setLottoId(lottoId);
        m.setTipo(TipoMovimento.valueOf(getString(rs, "tipo")));
        m.setQuantita(getBigDecimal(rs, "quantita"));
        m.setQuantitaPrecedente(getBigDecimal(rs, "quantita_precedente"));
        m.setQuantitaSuccessiva(getBigDecimal(rs, "quantita_successiva"));
        m.setCausale(getString(rs, "causale"));
        m.setDocumentoRif(getString(rs, "documento_rif"));
        Integer magDestId = getInteger(rs, "magazzino_destinazione_id");
        if (magDestId != null) m.setMagazzinoDestinazioneId(magDestId);
        Integer utenteId = getInteger(rs, "utente_id");
        if (utenteId != null) m.setUtenteId(utenteId);
        m.setNote(getString(rs, "note"));
        m.setDataMovimento(getLocalDateTime(rs, "data_movimento"));
        return m;
    }

    public List<Movimento> findByProdotto(int prodottoId) {
        return executeQuery(
                "SELECT * FROM movimenti WHERE prodotto_id = ? ORDER BY data_movimento DESC",
                prodottoId);
    }

    public List<Movimento> findByMagazzino(int magazzinoId) {
        return executeQuery(
                "SELECT * FROM movimenti WHERE magazzino_id = ? ORDER BY data_movimento DESC",
                magazzinoId);
    }

    public List<Movimento> findByPeriodo(LocalDate dataInizio, LocalDate dataFine) {
        return executeQuery(
                "SELECT * FROM movimenti WHERE DATE(data_movimento) BETWEEN ? AND ? ORDER BY data_movimento DESC",
                dataInizio, dataFine);
    }

    public List<Movimento> findByTipo(TipoMovimento tipo) {
        return executeQuery(
                "SELECT * FROM movimenti WHERE tipo = ? ORDER BY data_movimento DESC",
                tipo.name());
    }

    public List<Movimento> findRecent(int limit) {
        return executeQuery(
                "SELECT * FROM movimenti ORDER BY data_movimento DESC LIMIT ?",
                limit);
    }

    public List<MovimentoCompleto> findMovimentiCompleti(LocalDate dataInizio, LocalDate dataFine,
                                                          Integer magazzinoId, TipoMovimento tipo) {
        StringBuilder sql = new StringBuilder("""
            SELECT m.*, p.codice, p.nome as prodotto_nome, mag.nome as magazzino_nome,
                   u.username, magd.nome as magazzino_dest_nome
            FROM movimenti m
            JOIN prodotti p ON m.prodotto_id = p.id
            JOIN magazzini mag ON m.magazzino_id = mag.id
            LEFT JOIN utenti u ON m.utente_id = u.id
            LEFT JOIN magazzini magd ON m.magazzino_destinazione_id = magd.id
            WHERE 1=1
            """);

        java.util.List<Object> params = new java.util.ArrayList<>();

        if (dataInizio != null) {
            sql.append(" AND DATE(m.data_movimento) >= ?");
            params.add(dataInizio);
        }
        if (dataFine != null) {
            sql.append(" AND DATE(m.data_movimento) <= ?");
            params.add(dataFine);
        }
        if (magazzinoId != null) {
            sql.append(" AND m.magazzino_id = ?");
            params.add(magazzinoId);
        }
        if (tipo != null) {
            sql.append(" AND m.tipo = ?");
            params.add(tipo.name());
        }

        sql.append(" ORDER BY m.data_movimento DESC");

        return new java.util.ArrayList<>() {{
            try (var conn = getConnection();
                 var stmt = conn.prepareStatement(sql.toString())) {
                setParameters(stmt, params.toArray());
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        MovimentoCompleto mc = new MovimentoCompleto();
                        mc.setId(rs.getInt("id"));
                        mc.setProdottoId(rs.getInt("prodotto_id"));
                        mc.setMagazzinoId(rs.getInt("magazzino_id"));
                        mc.setTipo(TipoMovimento.valueOf(getString(rs, "tipo")));
                        mc.setQuantita(getBigDecimal(rs, "quantita"));
                        mc.setQuantitaPrecedente(getBigDecimal(rs, "quantita_precedente"));
                        mc.setQuantitaSuccessiva(getBigDecimal(rs, "quantita_successiva"));
                        mc.setCausale(getString(rs, "causale"));
                        mc.setDocumentoRif(getString(rs, "documento_rif"));
                        mc.setNote(getString(rs, "note"));
                        mc.setDataMovimento(getLocalDateTime(rs, "data_movimento"));
                        mc.setCodice(getString(rs, "codice"));
                        mc.setProdottoNome(getString(rs, "prodotto_nome"));
                        mc.setMagazzinoNome(getString(rs, "magazzino_nome"));
                        mc.setUsername(getString(rs, "username"));
                        mc.setMagazzinoDestNome(getString(rs, "magazzino_dest_nome"));
                        add(mc);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error finding movimenti completi: {}", e.getMessage());
            }
        }};
    }

    public int insert(Movimento movimento) {
        String sql = """
            INSERT INTO movimenti (prodotto_id, magazzino_id, lotto_id, tipo, quantita,
                quantita_precedente, quantita_successiva, causale, documento_rif,
                magazzino_destinazione_id, utente_id, note, data_movimento)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                movimento.getProdottoId(),
                movimento.getMagazzinoId(),
                movimento.getLottoId() > 0 ? movimento.getLottoId() : null,
                movimento.getTipo().name(),
                movimento.getQuantita(),
                movimento.getQuantitaPrecedente(),
                movimento.getQuantitaSuccessiva(),
                movimento.getCausale(),
                movimento.getDocumentoRif(),
                movimento.getMagazzinoDestinazioneId() > 0 ? movimento.getMagazzinoDestinazioneId() : null,
                movimento.getUtenteId() > 0 ? movimento.getUtenteId() : null,
                movimento.getNote(),
                movimento.getDataMovimento() != null ? movimento.getDataMovimento() : LocalDateTime.now());
    }

    public int countByPeriodo(LocalDate dataInizio, LocalDate dataFine) {
        String sql = "SELECT COUNT(*) FROM movimenti WHERE DATE(data_movimento) BETWEEN ? AND ?";
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(dataInizio));
            stmt.setDate(2, java.sql.Date.valueOf(dataFine));
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting movimenti: {}", e.getMessage());
        }
        return 0;
    }

    public static class MovimentoCompleto {
        private int id;
        private int prodottoId;
        private int magazzinoId;
        private TipoMovimento tipo;
        private BigDecimal quantita;
        private BigDecimal quantitaPrecedente;
        private BigDecimal quantitaSuccessiva;
        private String causale;
        private String documentoRif;
        private String note;
        private LocalDateTime dataMovimento;
        private String codice;
        private String prodottoNome;
        private String magazzinoNome;
        private String username;
        private String magazzinoDestNome;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getProdottoId() { return prodottoId; }
        public void setProdottoId(int prodottoId) { this.prodottoId = prodottoId; }
        public int getMagazzinoId() { return magazzinoId; }
        public void setMagazzinoId(int magazzinoId) { this.magazzinoId = magazzinoId; }
        public TipoMovimento getTipo() { return tipo; }
        public void setTipo(TipoMovimento tipo) { this.tipo = tipo; }
        public BigDecimal getQuantita() { return quantita; }
        public void setQuantita(BigDecimal quantita) { this.quantita = quantita; }
        public BigDecimal getQuantitaPrecedente() { return quantitaPrecedente; }
        public void setQuantitaPrecedente(BigDecimal quantitaPrecedente) { this.quantitaPrecedente = quantitaPrecedente; }
        public BigDecimal getQuantitaSuccessiva() { return quantitaSuccessiva; }
        public void setQuantitaSuccessiva(BigDecimal quantitaSuccessiva) { this.quantitaSuccessiva = quantitaSuccessiva; }
        public String getCausale() { return causale; }
        public void setCausale(String causale) { this.causale = causale; }
        public String getDocumentoRif() { return documentoRif; }
        public void setDocumentoRif(String documentoRif) { this.documentoRif = documentoRif; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public LocalDateTime getDataMovimento() { return dataMovimento; }
        public void setDataMovimento(LocalDateTime dataMovimento) { this.dataMovimento = dataMovimento; }
        public String getCodice() { return codice; }
        public void setCodice(String codice) { this.codice = codice; }
        public String getProdottoNome() { return prodottoNome; }
        public void setProdottoNome(String prodottoNome) { this.prodottoNome = prodottoNome; }
        public String getMagazzinoNome() { return magazzinoNome; }
        public void setMagazzinoNome(String magazzinoNome) { this.magazzinoNome = magazzinoNome; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getMagazzinoDestNome() { return magazzinoDestNome; }
        public void setMagazzinoDestNome(String magazzinoDestNome) { this.magazzinoDestNome = magazzinoDestNome; }
    }
}
