package br.com.fiap.gs.repository;

import br.com.fiap.gs.model.SessionToken;
import java.sql.*;
import java.util.Date;

public class SessionRepository {
    private final ConnectionFactory cf = new ConnectionFactory();

    public void salvar(SessionToken st) throws SQLException {
        String sql = "INSERT INTO SESSION_TOKENS (TOKEN, ID_USUARIO, EXPIRES_AT, CREATED_AT) VALUES (?, ?, ?, ?)";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, st.getToken());
            ps.setLong(2, st.getUserId());
            ps.setTimestamp(3, new Timestamp(st.getExpiresAt().getTime()));
            ps.setTimestamp(4, new Timestamp(st.getCreatedAt().getTime()));
            ps.executeUpdate();
        }
    }

    public Long validarToken(String token) throws SQLException {
        String sql = "SELECT ID_USUARIO, EXPIRES_AT FROM SESSION_TOKENS WHERE TOKEN = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, token);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Timestamp exp = rs.getTimestamp("EXPIRES_AT");
                    System.out.println("[SessionRepository] validarToken: token found=" + token + " user=" + rs.getLong("ID_USUARIO") + " expiresAt=" + exp);
                    if (exp != null && exp.after(new Timestamp(new Date().getTime()))) {
                        return rs.getLong("ID_USUARIO");
                    } else {
                        System.out.println("[SessionRepository] validarToken: token expired or invalid: " + token + " expiresAt=" + exp);
                    }
                } else {
                    System.out.println("[SessionRepository] validarToken: token NOT FOUND: " + token);
                }
            }
        }
        return null;
    }


    public void deletar(String token) throws SQLException {
        String sql = "DELETE FROM SESSION_TOKENS WHERE TOKEN = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, token);
            st.executeUpdate();
        }
    }

    public void deletarPorUserId(Long userId) throws SQLException {
        String sql = "DELETE FROM SESSION_TOKENS WHERE ID_USUARIO = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setLong(1, userId);
            st.executeUpdate();
        }
    }
}
