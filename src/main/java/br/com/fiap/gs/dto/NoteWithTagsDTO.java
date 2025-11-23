// src/main/java/br/com/fiap/gs/dto/NoteWithTagsDTO.java
package br.com.fiap.gs.dto;

import java.util.List;

public class NoteWithTagsDTO {
    public Long id;
    public String title;
    public String content;
    public String createdAt; // ou Timestamp se preferir
    public Long userId;
    public List<String> tags; // só nomes (frontend espera strings)

    public NoteWithTagsDTO() {}
}
