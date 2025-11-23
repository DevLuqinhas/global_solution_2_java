package br.com.fiap.gs.business;

import br.com.fiap.gs.model.Note;
import br.com.fiap.gs.model.Tag;
import br.com.fiap.gs.repository.NoteRepository;
import br.com.fiap.gs.repository.TagRepository;
import br.com.fiap.gs.dto.NoteWithTagsDTO;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NoteBusiness {

    private final NoteRepository noteRepo = new NoteRepository();
    private final TagRepository tagRepo = new TagRepository();

    // Criar nota com tags
    public Long criarNota(String titulo, String conteudo, List<String> tags, Long userId) throws Exception {
        return noteRepo.createNote(titulo, conteudo, userId, tags);
    }

    // Listar notas do usuário (entidades)
    public List<Note> listarPorUsuario(Long userId) throws Exception {
        if (userId == null) return Collections.emptyList();
        return noteRepo.listByUser(userId);
    }

    // Buscar nota por ID (entidade)
    public Note buscarPorId(Long id) throws Exception {
        return noteRepo.findById(id).orElse(null);
    }

    // Atualizar nota
    public boolean atualizar(Note note, Long userId) throws Exception {
        return noteRepo.updateNote(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getTags() != null
                        ? note.getTags().stream().map(Tag::getNome).toList()
                        : List.of(),
                userId
        );
    }

    // Deletar nota
    public boolean deletar(Long id, Long userId) throws Exception {
        return noteRepo.deleteNote(id, userId);
    }

    public List<NoteWithTagsDTO> listarPorUsuarioComTags(Long userId) throws Exception {
        List<Note> notes = listarPorUsuario(userId);
        return notes.stream().map(this::toDto).collect(Collectors.toList());
    }

    public NoteWithTagsDTO buscarPorIdComTags(Long id) throws Exception {
        Note n = buscarPorId(id);
        if (n == null) return null;
        return toDto(n);
    }

    private NoteWithTagsDTO toDto(Note n) {
        NoteWithTagsDTO dto = new NoteWithTagsDTO();
        dto.id = n.getId();
        dto.title = n.getTitle();
        dto.content = n.getContent();
        dto.createdAt = n.getCreatedAt() != null ? n.getCreatedAt().toString() : null;
        dto.userId = n.getUserId();
        if (n.getTags() != null) {
            dto.tags = n.getTags().stream()
                    .map(Tag::getNome)
                    .collect(Collectors.toList());
        } else {
            dto.tags = List.of();
        }
        return dto;
    }
}
