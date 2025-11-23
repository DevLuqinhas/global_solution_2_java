package br.com.fiap.gs.repository;

import br.com.fiap.gs.model.Note;
import br.com.fiap.gs.model.Tag;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class NoteRepository {
    private final ConnectionFactory cf = new ConnectionFactory();
    private final TagRepository tagRepo = new TagRepository();

    // CREATE note + vincula tags (recebe lista de nomes de tags)
    public Long createNote(String title, String content, Long userId, List<String> tagNames) throws SQLException {
        String insert = "INSERT INTO NOTES (TITLE, CONTENT, ID_USUARIO) VALUES (?, ?, ?)";
        Long noteId;

        Connection con = null;
        try {
            con = cf.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(insert, new String[]{"ID_NOTE"})) {
                ps.setString(1, title);
                ps.setString(2, content);
                ps.setLong(3, userId);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        noteId = keys.getLong(1);
                    } else {
                        throw new SQLException("Falha ao recuperar ID da nota");
                    }
                }
            }

            // vincular tags
            if (tagNames != null) {
                for (String t : tagNames) {
                    Tag tag = tagRepo.findOrCreate(t, userId);
                    linkNoteTag(con, noteId, tag.getId());
                }
            }

            con.commit();
            return noteId;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw e;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { /* ignore */ }
        }
    }

    // helper para inserir em NOTE_TAGS (usa conexão externa para transações)
    private void linkNoteTag(Connection con, Long noteId, Long tagId) throws SQLException {
        String sql = "INSERT INTO NOTE_TAGS (ID_NOTE, ID_TAG) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, noteId);
            ps.setLong(2, tagId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // se já existe relação, ignora (idempotência)
            if (!e.getMessage().contains("unique") && !e.getMessage().contains("PK_NOTE_TAGS")) {
                throw e;
            }
        }
    }

    // READ por id (traz tags)
    public Optional<Note> findById(Long id) throws SQLException {
        String sql = "SELECT ID_NOTE, TITLE, CONTENT, CREATED_AT, ID_USUARIO FROM NOTES WHERE ID_NOTE = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Note n = new Note();
                    n.setId(rs.getLong("ID_NOTE"));
                    n.setTitle(rs.getString("TITLE"));
                    n.setContent(rs.getString("CONTENT"));
                    Timestamp ts = rs.getTimestamp("CREATED_AT");
                    if (ts != null) n.setCreatedAt(new Date(ts.getTime()));
                    n.setUserId(rs.getLong("ID_USUARIO"));
                    n.setTags(getTagsForNote(n.getId()));
                    return Optional.of(n);
                }
            }
        }
        return Optional.empty();
    }

    // LIST notas do usuário com tags
    public List<Note> listByUser(Long userId) throws SQLException {
        List<Note> out = new ArrayList<>();
        String sql = "SELECT ID_NOTE, TITLE, CONTENT, CREATED_AT FROM NOTES WHERE ID_USUARIO = ? ORDER BY CREATED_AT DESC";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Note n = new Note();
                    n.setId(rs.getLong("ID_NOTE"));
                    n.setTitle(rs.getString("TITLE"));
                    n.setContent(rs.getString("CONTENT"));
                    Timestamp ts = rs.getTimestamp("CREATED_AT");
                    if (ts != null) n.setCreatedAt(new Date(ts.getTime()));
                    n.setUserId(userId);
                    n.setTags(getTagsForNote(n.getId()));
                    out.add(n);
                }
            }
        }
        return out;
    }

    // UPDATE note (title/content) and sync tags
    public boolean updateNote(Long noteId, String newTitle, String newContent, List<String> newTagNames, Long userId) throws SQLException {
        Connection con = null;
        try {
            con = cf.getConnection();
            con.setAutoCommit(false);

            // atualiza fields
            String sql = "UPDATE NOTES SET TITLE = ?, CONTENT = ? WHERE ID_NOTE = ? AND ID_USUARIO = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, newTitle);
                ps.setString(2, newContent);
                ps.setLong(3, noteId);
                ps.setLong(4, userId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    con.rollback();
                    return false;
                }
            }

            // sincronizar tags: simplificação - remover todas e inserir novamente
            String deleteRel = "DELETE FROM NOTE_TAGS WHERE ID_NOTE = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteRel)) {
                ps.setLong(1, noteId);
                ps.executeUpdate();
            }

            if (newTagNames != null) {
                for (String t : newTagNames) {
                    Tag tag = tagRepo.findOrCreate(t, userId);
                    linkNoteTag(con, noteId, tag.getId());
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) {}
            throw e;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) {}
        }
    }

    // DELETE note (NOTE_TAGS tem FK ON DELETE CASCADE se sua DDL tiver)
    public boolean deleteNote(Long noteId, Long userId) throws SQLException {
        String sql = "DELETE FROM NOTES WHERE ID_NOTE = ? AND ID_USUARIO = ?";
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, noteId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // pega tags associadas a uma nota
    private List<Tag> getTagsForNote(Long noteId) throws SQLException {
        List<Tag> out = new ArrayList<>();
        String sql = """
            SELECT t.ID_TAG, t.NOME, t.ID_USUARIO
            FROM TAGS t
            JOIN NOTE_TAGS nt ON nt.ID_TAG = t.ID_TAG
            WHERE nt.ID_NOTE = ?
            ORDER BY t.NOME_NORMALIZADO
        """;
        try (Connection con = cf.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, noteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Tag(rs.getLong("ID_TAG"), rs.getString("NOME"), rs.getLong("ID_USUARIO")));
                }
            }
        }
        return out;
    }
}
