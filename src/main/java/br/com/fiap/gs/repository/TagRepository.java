package br.com.fiap.gs.repository;

import br.com.fiap.gs.model.Tag;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TagRepository {
    private final ConnectionFactory cf = new ConnectionFactory();

    // encontra por id
    public Optional<Tag> findById(Long id) throws SQLException {
        String sql = "SELECT ID_TAG, NOME, ID_USUARIO FROM TAGS WHERE ID_TAG = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Tag(
                            rs.getLong("ID_TAG"),
                            rs.getString("NOME"),
                            rs.getLong("ID_USUARIO")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    // encontra tag por nome normalizado + user (nome deve ser fornecido livre)
    public Optional<Tag> findByNameAndUser(String nome, Long userId) throws SQLException {
        String sql = "SELECT ID_TAG, NOME FROM TAGS WHERE NOME_NORMALIZADO = ? AND ID_USUARIO = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nome.trim().toLowerCase());
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Tag(rs.getLong("ID_TAG"), rs.getString("NOME"), userId));
                }
            }
        }
        return Optional.empty();
    }

    // criar tag (retorna Tag com id)
    public Tag create(String nome, Long userId) throws SQLException {
        String insert = "INSERT INTO TAGS (NOME, ID_USUARIO) VALUES (?, ?)";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(insert, new String[]{"ID_TAG"})) {

            ps.setString(1, nome.trim());
            ps.setLong(2, userId);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    return new Tag(id, nome.trim(), userId);
                }
            }
        }
        throw new SQLException("Failed to create tag");
    }

    // encontrar ou criar (transacionalmente simples)
    public Tag findOrCreate(String nome, Long userId) throws SQLException {
        Optional<Tag> found = findByNameAndUser(nome, userId);
        if (found.isPresent()) return found.get();
        return create(nome, userId);
    }

    // listar tags de um usuário (ordenadas)
    public List<Tag> listByUser(Long userId) throws SQLException {
        List<Tag> out = new ArrayList<>();
        String sql = "SELECT ID_TAG, NOME FROM TAGS WHERE ID_USUARIO = ? ORDER BY NOME_NORMALIZADO";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Tag(rs.getLong("ID_TAG"), rs.getString("NOME"), userId));
                }
            }
        }
        return out;
    }

    // atualizar nome da tag (mantendo unicidade; lança SQLException se violar unique)
    public boolean update(Long tagId, String novoNome) throws SQLException {
        String sql = "UPDATE TAGS SET NOME = ? WHERE ID_TAG = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, novoNome.trim());
            ps.setLong(2, tagId);
            return ps.executeUpdate() > 0;
        }
    }

    // deletar tag (irá remover relação por cascade se NOTE_TAGS FK tiver ON DELETE CASCADE em TAGS)
    public boolean delete(Long tagId) throws SQLException {
        String sql = "DELETE FROM TAGS WHERE ID_TAG = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, tagId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long tagId, Long userId) throws SQLException {
        String delNoteTags = "DELETE FROM NOTE_TAGS WHERE ID_TAG = ?";
        String delTag = "DELETE FROM TAGS WHERE ID_TAG = ? AND ID_USUARIO = ?";
        try (Connection con = cf.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement p1 = con.prepareStatement(delNoteTags)) {
                p1.setLong(1, tagId);
                p1.executeUpdate();
            }
            try (PreparedStatement p2 = con.prepareStatement(delTag)) {
                p2.setLong(1, tagId);
                p2.setLong(2, userId);
                int removed = p2.executeUpdate();
                con.commit();
                return removed > 0;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

}
