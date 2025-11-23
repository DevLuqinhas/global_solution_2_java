package br.com.fiap.gs.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ContatoDTO {
    private String nome;
    private String email;
    private String mensagem;
    private Long id_usuario; // opcional: vincular ao usuário autenticado

    public ContatoDTO() {}

    public ContatoDTO(String nome, String email, String mensagem, Long id_usuario) {
        this.nome = nome;
        this.email = email;
        this.mensagem = mensagem;
        this.id_usuario = id_usuario;
    }

    // Getters / Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public Long getId_usuario() { return id_usuario; }
    public void setId_usuario(Long id_usuario) { this.id_usuario = id_usuario; }

    public boolean isValid() {
        return nome != null && !nome.isBlank()
                && email != null && email.contains("@")
                && mensagem != null && !mensagem.isBlank();
    }
}
