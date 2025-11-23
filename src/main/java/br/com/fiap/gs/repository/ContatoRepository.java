package br.com.fiap.gs.repository;

import br.com.fiap.gs.model.Contato;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContatoRepository {

    private ConnectionFactory cf = new ConnectionFactory();

    // CREATE
    public void salvar(Contato contato) throws SQLException {
        String sql = """
            INSERT INTO CONTACTS
            (name, email, message, data_send, status_contact, id_usuario)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, contato.getNome());
            st.setString(2, contato.getEmail());
            st.setString(3, contato.getMensagem());
            st.setTimestamp(4, Timestamp.valueOf(contato.getDataSend()));
            st.setInt(5, contato.getStatus_contact());
            if (contato.getId_usuario() != null)
                st.setLong(6, contato.getId_usuario());
            else
                st.setNull(6, Types.BIGINT);

            st.executeUpdate();
        }
    }

    // READ ALL
    public List<Contato> buscarTodas() throws SQLException {
        List<Contato> lista = new ArrayList<>();
        String sql = "SELECT * FROM CONTACTS ORDER BY data_send DESC";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // READ by id
    public Contato buscarPorId(long id) throws SQLException {
        String sql = "SELECT * FROM CONTACTS WHERE id_contact = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // UPDATE
    public boolean update(Contato contato) throws SQLException {
        String sql = """
            UPDATE CONTACTS
            SET name=?, email=?, message=?, status_contact=?, id_usuario=?
            WHERE id_contact=?
        """;
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, contato.getNome());
            st.setString(2, contato.getEmail());
            st.setString(3, contato.getMensagem());
            st.setInt(4, contato.getStatus_contact());
            if (contato.getId_usuario() != null)
                st.setLong(5, contato.getId_usuario());
            else
                st.setNull(5, Types.BIGINT);
            st.setLong(6, contato.getId_contact());

            return st.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean deletarPorId(long id) throws SQLException {
        String sql = "DELETE FROM CONTACTS WHERE id_contact = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setLong(1, id);
            return st.executeUpdate() > 0;
        }
    }

    private Contato mapear(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("data_send");
        Contato c = new Contato(
                rs.getLong("id_contact"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("message"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getInt("status_contact")
        );
        long idUsuario = rs.getLong("id_usuario");
        if (!rs.wasNull()) c.setId_usuario(idUsuario);
        return c;
    }
}
