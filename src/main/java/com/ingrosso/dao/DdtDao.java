package com.ingrosso.dao;

import com.ingrosso.model.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DdtDao extends BaseDao<Ddt> {

    @Override
    protected String getTableName() {
        return "ddt";
    }

    @Override
    protected Ddt mapResultSet(ResultSet rs) throws SQLException {
        Ddt d = new Ddt();
        d.setId(rs.getInt("id"));
        d.setNumero(rs.getInt("numero"));
        d.setAnno(rs.getInt("anno"));
        d.setDataDocumento(getLocalDate(rs, "data_documento"));
        d.setDataTrasporto(getLocalDateTime(rs, "data_trasporto"));
        Integer destId = getInteger(rs, "destinatario_id");
        if (destId != null) d.setDestinatarioId(destId);
        d.setDestinazioneDiversa(getString(rs, "destinazione_diversa"));
        d.setMagazzinoId(rs.getInt("magazzino_id"));
        d.setCausaleTrasporto(getString(rs, "causale_trasporto"));
        d.setAspettoBeni(getString(rs, "aspetto_beni"));
        d.setColli(rs.getInt("colli"));
        d.setPesoKg(getBigDecimal(rs, "peso_kg"));
        d.setPorto(getString(rs, "porto"));
        d.setVettore(getString(rs, "vettore"));
        d.setNote(getString(rs, "note"));
        Integer utenteId = getInteger(rs, "utente_id");
        if (utenteId != null) d.setUtenteId(utenteId);
        d.setStato(StatoDdt.valueOf(getString(rs, "stato")));
        d.setCreatedAt(getLocalDateTime(rs, "created_at"));
        return d;
    }

    public List<Ddt> findByAnno(int anno) {
        return executeQuery("SELECT * FROM ddt WHERE anno = ? ORDER BY numero DESC", anno);
    }

    public List<Ddt> findByPeriodo(LocalDate dataInizio, LocalDate dataFine) {
        return executeQuery(
                "SELECT * FROM ddt WHERE data_documento BETWEEN ? AND ? ORDER BY data_documento DESC, numero DESC",
                dataInizio, dataFine);
    }

    public List<Ddt> findByDestinatario(int destinatarioId) {
        return executeQuery(
                "SELECT * FROM ddt WHERE destinatario_id = ? ORDER BY data_documento DESC, numero DESC",
                destinatarioId);
    }

    public List<Ddt> findByStato(StatoDdt stato) {
        return executeQuery("SELECT * FROM ddt WHERE stato = ? ORDER BY data_documento DESC, numero DESC", stato.name());
    }

    public Optional<Ddt> findByNumeroAnno(int numero, int anno) {
        return executeSingleQuery("SELECT * FROM ddt WHERE numero = ? AND anno = ?", numero, anno);
    }

    public int getNextNumero(int anno) {
        String sql = """
            INSERT INTO numeratori (tipo, anno, ultimo_numero) VALUES ('DDT', ?, 1)
            ON DUPLICATE KEY UPDATE ultimo_numero = ultimo_numero + 1
            """;
        executeUpdate(sql, anno);

        String selectSql = "SELECT ultimo_numero FROM numeratori WHERE tipo = 'DDT' AND anno = ?";
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, anno);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting next DDT number: {}", e.getMessage());
        }
        return 1;
    }

    public int insert(Ddt ddt) {
        String sql = """
            INSERT INTO ddt (numero, anno, data_documento, data_trasporto, destinatario_id,
                destinazione_diversa, magazzino_id, causale_trasporto, aspetto_beni, colli,
                peso_kg, porto, vettore, note, utente_id, stato)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                ddt.getNumero(),
                ddt.getAnno(),
                ddt.getDataDocumento(),
                ddt.getDataTrasporto(),
                ddt.getDestinatarioId() > 0 ? ddt.getDestinatarioId() : null,
                ddt.getDestinazioneDiversa(),
                ddt.getMagazzinoId(),
                ddt.getCausaleTrasporto(),
                ddt.getAspettoBeni(),
                ddt.getColli(),
                ddt.getPesoKg(),
                ddt.getPorto(),
                ddt.getVettore(),
                ddt.getNote(),
                ddt.getUtenteId() > 0 ? ddt.getUtenteId() : null,
                ddt.getStato().name());
    }

    public boolean update(Ddt ddt) {
        String sql = """
            UPDATE ddt SET data_documento = ?, data_trasporto = ?, destinatario_id = ?,
                destinazione_diversa = ?, magazzino_id = ?, causale_trasporto = ?, aspetto_beni = ?,
                colli = ?, peso_kg = ?, porto = ?, vettore = ?, note = ?, stato = ?
            WHERE id = ?
            """;
        return executeUpdate(sql,
                ddt.getDataDocumento(),
                ddt.getDataTrasporto(),
                ddt.getDestinatarioId() > 0 ? ddt.getDestinatarioId() : null,
                ddt.getDestinazioneDiversa(),
                ddt.getMagazzinoId(),
                ddt.getCausaleTrasporto(),
                ddt.getAspettoBeni(),
                ddt.getColli(),
                ddt.getPesoKg(),
                ddt.getPorto(),
                ddt.getVettore(),
                ddt.getNote(),
                ddt.getStato().name(),
                ddt.getId()) > 0;
    }

    public boolean updateStato(int ddtId, StatoDdt stato) {
        return executeUpdate("UPDATE ddt SET stato = ? WHERE id = ?", stato.name(), ddtId) > 0;
    }

    // DDT Righe methods
    public List<DdtRiga> findRigheByDdt(int ddtId) {
        String sql = "SELECT * FROM ddt_righe WHERE ddt_id = ? ORDER BY ordine";
        return new java.util.ArrayList<>() {{
            try (var conn = getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, ddtId);
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        DdtRiga r = new DdtRiga();
                        r.setId(rs.getInt("id"));
                        r.setDdtId(rs.getInt("ddt_id"));
                        r.setProdottoId(rs.getInt("prodotto_id"));
                        Integer lottoId = getInteger(rs, "lotto_id");
                        if (lottoId != null) r.setLottoId(lottoId);
                        r.setDescrizione(getString(rs, "descrizione"));
                        r.setQuantita(getBigDecimal(rs, "quantita"));
                        r.setUnitaMisura(getString(rs, "unita_misura"));
                        r.setPrezzoUnitario(getBigDecimal(rs, "prezzo_unitario"));
                        r.setAliquotaIva(getBigDecimal(rs, "aliquota_iva"));
                        r.setOrdine(rs.getInt("ordine"));
                        add(r);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error finding DDT righe: {}", e.getMessage());
            }
        }};
    }

    public int insertRiga(DdtRiga riga) {
        String sql = """
            INSERT INTO ddt_righe (ddt_id, prodotto_id, lotto_id, descrizione, quantita,
                unita_misura, prezzo_unitario, aliquota_iva, ordine)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                riga.getDdtId(),
                riga.getProdottoId(),
                riga.getLottoId() > 0 ? riga.getLottoId() : null,
                riga.getDescrizione(),
                riga.getQuantita(),
                riga.getUnitaMisura(),
                riga.getPrezzoUnitario(),
                riga.getAliquotaIva(),
                riga.getOrdine());
    }

    public boolean updateRiga(DdtRiga riga) {
        String sql = """
            UPDATE ddt_righe SET prodotto_id = ?, lotto_id = ?, descrizione = ?, quantita = ?,
                unita_misura = ?, prezzo_unitario = ?, aliquota_iva = ?, ordine = ?
            WHERE id = ?
            """;
        return executeUpdate(sql,
                riga.getProdottoId(),
                riga.getLottoId() > 0 ? riga.getLottoId() : null,
                riga.getDescrizione(),
                riga.getQuantita(),
                riga.getUnitaMisura(),
                riga.getPrezzoUnitario(),
                riga.getAliquotaIva(),
                riga.getOrdine(),
                riga.getId()) > 0;
    }

    public boolean deleteRiga(int rigaId) {
        return executeUpdate("DELETE FROM ddt_righe WHERE id = ?", rigaId) > 0;
    }

    public boolean deleteRigheByDdt(int ddtId) {
        return executeUpdate("DELETE FROM ddt_righe WHERE ddt_id = ?", ddtId) >= 0;
    }

    // Destinatari methods
    public List<Destinatario> findAllDestinatari() {
        String sql = "SELECT * FROM destinatari WHERE attivo = TRUE ORDER BY ragione_sociale";
        return executeDestinatariQuery(sql);
    }

    public Optional<Destinatario> findDestinatarioById(int id) {
        String sql = "SELECT * FROM destinatari WHERE id = ?";
        List<Destinatario> list = executeDestinatariQuery(sql, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Destinatario> searchDestinatari(String text) {
        String pattern = "%" + text + "%";
        String sql = "SELECT * FROM destinatari WHERE (ragione_sociale LIKE ? OR codice LIKE ?) AND attivo = TRUE ORDER BY ragione_sociale";
        return executeDestinatariQuery(sql, pattern, pattern);
    }

    public int insertDestinatario(Destinatario dest) {
        String sql = """
            INSERT INTO destinatari (codice, ragione_sociale, indirizzo, citta, cap, provincia,
                piva, codice_fiscale, telefono, email, note, attivo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        return executeInsert(sql,
                dest.getCodice(),
                dest.getRagioneSociale(),
                dest.getIndirizzo(),
                dest.getCitta(),
                dest.getCap(),
                dest.getProvincia(),
                dest.getPiva(),
                dest.getCodiceFiscale(),
                dest.getTelefono(),
                dest.getEmail(),
                dest.getNote(),
                dest.isAttivo());
    }

    public boolean updateDestinatario(Destinatario dest) {
        String sql = """
            UPDATE destinatari SET codice = ?, ragione_sociale = ?, indirizzo = ?, citta = ?,
                cap = ?, provincia = ?, piva = ?, codice_fiscale = ?, telefono = ?, email = ?,
                note = ?, attivo = ?
            WHERE id = ?
            """;
        return executeUpdate(sql,
                dest.getCodice(),
                dest.getRagioneSociale(),
                dest.getIndirizzo(),
                dest.getCitta(),
                dest.getCap(),
                dest.getProvincia(),
                dest.getPiva(),
                dest.getCodiceFiscale(),
                dest.getTelefono(),
                dest.getEmail(),
                dest.getNote(),
                dest.isAttivo(),
                dest.getId()) > 0;
    }

    private List<Destinatario> executeDestinatariQuery(String sql, Object... params) {
        return new java.util.ArrayList<>() {{
            try (var conn = getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Destinatario d = new Destinatario();
                        d.setId(rs.getInt("id"));
                        d.setCodice(getString(rs, "codice"));
                        d.setRagioneSociale(getString(rs, "ragione_sociale"));
                        d.setIndirizzo(getString(rs, "indirizzo"));
                        d.setCitta(getString(rs, "citta"));
                        d.setCap(getString(rs, "cap"));
                        d.setProvincia(getString(rs, "provincia"));
                        d.setPiva(getString(rs, "piva"));
                        d.setCodiceFiscale(getString(rs, "codice_fiscale"));
                        d.setTelefono(getString(rs, "telefono"));
                        d.setEmail(getString(rs, "email"));
                        d.setNote(getString(rs, "note"));
                        d.setAttivo(getBoolean(rs, "attivo"));
                        d.setCreatedAt(getLocalDateTime(rs, "created_at"));
                        add(d);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error executing destinatari query: {}", e.getMessage());
            }
        }};
    }

    public int countByMeseAnno(int mese, int anno) {
        String sql = "SELECT COUNT(*) FROM ddt WHERE MONTH(data_documento) = ? AND YEAR(data_documento) = ? AND stato = 'EMESSO'";
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mese);
            stmt.setInt(2, anno);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting DDT: {}", e.getMessage());
        }
        return 0;
    }
}
