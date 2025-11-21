package br.com.fiap.gs.repository;

import br.com.fiap.gs.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final ConnectionFactory cf = new ConnectionFactory();

    public Long salvarRetornandoId(User user) throws SQLException {
        String sql = "INSERT INTO USERS (NOME, EMAIL, SENHA_HASH) VALUES (?, ?, ?)";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, user.getNome());
            st.setString(2, user.getEmail());
            st.setString(3, user.getSenhaHash());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        // fallback
        User p = buscarPorEmail(user.getEmail());
        return p != null ? p.getId() : null;
    }

    public User buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT ID_USUARIO, NOME, EMAIL, SENHA_HASH, DATA_CRIACAO FROM USERS WHERE EMAIL = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public User buscarPorId(long id) throws SQLException {
        String sql = "SELECT ID_USUARIO, NOME, EMAIL, SENHA_HASH, DATA_CRIACAO FROM USERS WHERE ID_USUARIO = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<User> buscarTodos() throws SQLException {
        List<User> lista = new ArrayList<>();
        String sql = "SELECT ID_USUARIO, NOME, EMAIL, SENHA_HASH, DATA_CRIACAO FROM USERS ORDER BY DATA_CRIACAO DESC";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public boolean atualizar(User user) throws SQLException {
        String sql = "UPDATE USERS SET NOME = ?, EMAIL = ?, SENHA_HASH = ? WHERE ID_USUARIO = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, user.getNome());
            st.setString(2, user.getEmail());
            st.setString(3, user.getSenhaHash());
            st.setLong(4, user.getId());
            return st.executeUpdate() > 0;
        }
    }

    public boolean deletarPorId(long id) throws SQLException {
        String sql = "DELETE FROM USERS WHERE ID_USUARIO = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setLong(1, id);
            return st.executeUpdate() > 0;
        }
    }

    private User mapear(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("ID_USUARIO"));
        u.setNome(rs.getString("NOME"));
        u.setEmail(rs.getString("EMAIL"));
        u.setSenhaHash(rs.getString("SENHA_HASH"));
        Timestamp ts = rs.getTimestamp("DATA_CRIACAO");
        if (ts != null) u.setDataCriacao(new java.util.Date(ts.getTime()));
        return u;
    }
}
