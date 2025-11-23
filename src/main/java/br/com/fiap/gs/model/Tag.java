package br.com.fiap.gs.model;

public class Tag {
    private Long id;
    private String nome;
    private Long userId;

    public Tag() {}

    public Tag(Long id, String nome, Long userId) {
        this.id = id;
        this.nome = nome;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
