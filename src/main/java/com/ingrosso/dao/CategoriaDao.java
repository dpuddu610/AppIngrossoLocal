package com.ingrosso.dao;

import com.ingrosso.model.Categoria;
import com.ingrosso.model.Sottocategoria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CategoriaDao extends BaseDao<Categoria> {

    @Override
    protected String getTableName() {
        return "categorie";
    }

    @Override
    protected Categoria mapResultSet(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getInt("id"));
        c.setNome(getString(rs, "nome"));
        c.setDescrizione(getString(rs, "descrizione"));
        c.setOrdine(rs.getInt("ordine"));
        c.setAttiva(getBoolean(rs, "attiva"));
        return c;
    }

    public List<Categoria> findAllActive() {
        return executeQuery("SELECT * FROM categorie WHERE attiva = TRUE ORDER BY ordine, nome");
    }

    public List<Categoria> findAllOrdered() {
        return executeQuery("SELECT * FROM categorie ORDER BY ordine, nome");
    }

    public int insert(Categoria categoria) {
        String sql = "INSERT INTO categorie (nome, descrizione, ordine, attiva) VALUES (?, ?, ?, ?)";
        return executeInsert(sql,
                categoria.getNome(),
                categoria.getDescrizione(),
                categoria.getOrdine(),
                categoria.isAttiva());
    }

    public boolean update(Categoria categoria) {
        String sql = "UPDATE categorie SET nome = ?, descrizione = ?, ordine = ?, attiva = ? WHERE id = ?";
        return executeUpdate(sql,
                categoria.getNome(),
                categoria.getDescrizione(),
                categoria.getOrdine(),
                categoria.isAttiva(),
                categoria.getId()) > 0;
    }

    // Sottocategorie methods
    public List<Sottocategoria> findSottocategorieByCategoria(int categoriaId) {
        String sql = "SELECT * FROM sottocategorie WHERE categoria_id = ? ORDER BY ordine, nome";
        return executeSottocategorieQuery(sql, categoriaId);
    }

    public List<Sottocategoria> findAllSottocategorieActive() {
        String sql = "SELECT * FROM sottocategorie WHERE attiva = TRUE ORDER BY ordine, nome";
        return executeSottocategorieQuery(sql);
    }

    public Sottocategoria findSottocategoriaById(int id) {
        String sql = "SELECT * FROM sottocategorie WHERE id = ?";
        List<Sottocategoria> list = executeSottocategorieQuery(sql, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insertSottocategoria(Sottocategoria sottocategoria) {
        String sql = "INSERT INTO sottocategorie (categoria_id, nome, descrizione, ordine, attiva) VALUES (?, ?, ?, ?, ?)";
        return executeInsert(sql,
                sottocategoria.getCategoriaId(),
                sottocategoria.getNome(),
                sottocategoria.getDescrizione(),
                sottocategoria.getOrdine(),
                sottocategoria.isAttiva());
    }

    public boolean updateSottocategoria(Sottocategoria sottocategoria) {
        String sql = "UPDATE sottocategorie SET categoria_id = ?, nome = ?, descrizione = ?, ordine = ?, attiva = ? WHERE id = ?";
        return executeUpdate(sql,
                sottocategoria.getCategoriaId(),
                sottocategoria.getNome(),
                sottocategoria.getDescrizione(),
                sottocategoria.getOrdine(),
                sottocategoria.isAttiva(),
                sottocategoria.getId()) > 0;
    }

    public boolean deleteSottocategoria(int id) {
        return executeUpdate("DELETE FROM sottocategorie WHERE id = ?", id) > 0;
    }

    private List<Sottocategoria> executeSottocategorieQuery(String sql, Object... params) {
        return new java.util.ArrayList<>() {{
            try (var conn = getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Sottocategoria sc = new Sottocategoria();
                        sc.setId(rs.getInt("id"));
                        sc.setCategoriaId(rs.getInt("categoria_id"));
                        sc.setNome(getString(rs, "nome"));
                        sc.setDescrizione(getString(rs, "descrizione"));
                        sc.setOrdine(rs.getInt("ordine"));
                        sc.setAttiva(getBoolean(rs, "attiva"));
                        add(sc);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error executing sottocategorie query: {}", e.getMessage());
            }
        }};
    }
}
