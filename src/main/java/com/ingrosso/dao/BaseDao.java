package com.ingrosso.dao;

import com.ingrosso.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseDao<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract T mapResultSet(ResultSet rs) throws SQLException;
    protected abstract String getTableName();

    protected Connection getConnection() throws SQLException {
        return DatabaseUtil.getConnection();
    }

    public Optional<T> findById(int id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding by id {}: {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTableName();
        return executeQuery(sql);
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            logger.error("Error deleting id {}: {}", id, e.getMessage());
            return false;
        }
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting: {}", e.getMessage());
        }
        return 0;
    }

    protected List<T> executeQuery(String sql, Object... params) {
        List<T> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing query: {}", e.getMessage());
        }
        return results;
    }

    protected Optional<T> executeSingleQuery(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing single query: {}", e.getMessage());
        }
        return Optional.empty();
    }

    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error executing update: {}", e.getMessage());
            return -1;
        }
    }

    protected int executeInsert(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, params);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing insert: {}", e.getMessage());
        }
        return -1;
    }

    protected void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int index = i + 1;

            if (param == null) {
                stmt.setNull(index, Types.NULL);
            } else if (param instanceof String) {
                stmt.setString(index, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(index, (Integer) param);
            } else if (param instanceof Long) {
                stmt.setLong(index, (Long) param);
            } else if (param instanceof Double) {
                stmt.setDouble(index, (Double) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(index, (Boolean) param);
            } else if (param instanceof java.math.BigDecimal) {
                stmt.setBigDecimal(index, (java.math.BigDecimal) param);
            } else if (param instanceof java.time.LocalDate) {
                stmt.setDate(index, java.sql.Date.valueOf((java.time.LocalDate) param));
            } else if (param instanceof java.time.LocalDateTime) {
                stmt.setTimestamp(index, java.sql.Timestamp.valueOf((java.time.LocalDateTime) param));
            } else if (param instanceof byte[]) {
                stmt.setBytes(index, (byte[]) param);
            } else if (param instanceof Enum) {
                stmt.setString(index, ((Enum<?>) param).name());
            } else {
                stmt.setObject(index, param);
            }
        }
    }

    protected String getString(ResultSet rs, String column) throws SQLException {
        return rs.getString(column);
    }

    protected Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    protected java.math.BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        return rs.getBigDecimal(column);
    }

    protected Boolean getBoolean(ResultSet rs, String column) throws SQLException {
        return rs.getBoolean(column);
    }

    protected java.time.LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        java.sql.Date date = rs.getDate(column);
        return date != null ? date.toLocalDate() : null;
    }

    protected java.time.LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    protected byte[] getBytes(ResultSet rs, String column) throws SQLException {
        return rs.getBytes(column);
    }
}
