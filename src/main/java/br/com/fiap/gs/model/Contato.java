package br.com.fiap.gs.model;

import java.time.LocalDateTime;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Contato {
    private long id_contact;
    private String nome;
    private String email;
    private String mensagem;
    private LocalDateTime dataSend;
    private int status_contact;
    private Long id_usuario; // opcional, pode ser null

    public Contato() {}

    public Contato(long id_contact, String nome, String email, String mensagem, LocalDateTime dataSend, int status_contact) {
        this.id_contact = id_contact;
        this.nome = nome;
        this.email = email;
        this.mensagem = mensagem;
        this.dataSend = dataSend;
        this.status_contact = status_contact;
    }

    // Getters e Setters
    public long getId_contact() { return id_contact; }
    public void setId_contact(long id_contact) { this.id_contact = id_contact; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public LocalDateTime getDataSend() { return dataSend; }
    public void setDataSend(LocalDateTime dataSend) { this.dataSend = dataSend; }

    public int getStatus_contact() { return status_contact; }
    public void setStatus_contact(int status_contact) { this.status_contact = status_contact; }

    public Long getId_usuario() { return id_usuario; }
    public void setId_usuario(Long id_usuario) { this.id_usuario = id_usuario; }
}
