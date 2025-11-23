package br.com.fiap.gs.business;

import br.com.fiap.gs.model.Tag;
import br.com.fiap.gs.repository.TagRepository;

import java.sql.SQLException;
import java.util.List;

public class TagBusiness {

    private final TagRepository repo = new TagRepository();

    public TagBusiness() {}

    // encontra ou cria tag para usuário (validações simples)
    public Tag encontrarOuCriar(String nome, Long userId) throws Exception {
        if (nome == null || nome.trim().isEmpty()) throw new IllegalArgumentException("Nome da tag inválido");
        if (userId == null) throw new IllegalArgumentException("Usuário inválido");

        try {
            return repo.findOrCreate(nome, userId);
        } catch (SQLException e) {
            throw new Exception("Erro ao criar/encontrar tag: " + e.getMessage(), e);
        }
    }

    // lista todas tags do usuário
    public List<Tag> listar(Long userId) throws Exception {
        if (userId == null) return java.util.Collections.emptyList();
        try {
            return repo.listByUser(userId);
        } catch (SQLException e) {
            throw new Exception("Erro ao listar tags: " + e.getMessage(), e);
        }
    }

    // deletar tag (só dono pode deletar)
    public boolean deletar(Long tagId, Long userId) throws Exception {
        if (tagId == null) throw new IllegalArgumentException("Tag id inválido");
        if (userId == null) throw new IllegalArgumentException("Usuário inválido");

        try {
            return repo.deletar(tagId, userId);
        } catch (SQLException e) {
            throw new Exception("Erro ao deletar tag: " + e.getMessage(), e);
        }
    }
}
