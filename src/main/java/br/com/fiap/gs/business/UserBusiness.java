package br.com.fiap.gs.business;

import br.com.fiap.gs.model.User;
import br.com.fiap.gs.model.SessionToken;
import br.com.fiap.gs.repository.UserRepository;
import br.com.fiap.gs.repository.SessionRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.List;

public class UserBusiness {
    private final UserRepository userRepo = new UserRepository();
    private final SessionRepository sessionRepo = new SessionRepository();

    public Long registrar(String nome, String email, String senhaPlain) throws Exception {
        if (userRepo.buscarPorEmail(email) != null) throw new Exception("Email já cadastrado");
        String hash = BCrypt.hashpw(senhaPlain, BCrypt.gensalt(12));
        User u = new User();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenhaHash(hash);
        return userRepo.salvarRetornandoId(u);
    }

    public AuthResult registrarEAutenticar(String nome, String email, String senhaPlain, int horasValidade) throws Exception {
        Long id = registrar(nome, email, senhaPlain);
        String token = criarSessaoPersistida(id, horasValidade);
        return new AuthResult(id, token, nome, email);
    }

    public User login(String email, String senhaPlain) throws Exception {
        User u = userRepo.buscarPorEmail(email);
        if (u == null) throw new Exception("Email ou senha inválidos");
        boolean ok = BCrypt.checkpw(senhaPlain, u.getSenhaHash());
        if (!ok) throw new Exception("Email ou senha inválidos");
        return u;
    }

    public AuthResult loginEAutenticar(String email, String senhaPlain, int horasValidade) throws Exception {
        User u = login(email, senhaPlain);
        String token = criarSessaoPersistida(u.getId(), horasValidade);
        return new AuthResult(u.getId(), token, u.getNome(), u.getEmail());
    }

    private String criarSessaoPersistida(Long userId, int horasValidade) throws Exception {
        String token = UUID.randomUUID().toString();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, horasValidade);
        Date expires = c.getTime();
        SessionToken st = new SessionToken();
        st.setToken(token);
        st.setUserId(userId);
        st.setCreatedAt(new Date());
        st.setExpiresAt(expires);
        sessionRepo.salvar(st);
        return token;
    }

    public Long validarToken(String token) throws Exception {
        if (token == null) return null;
        return sessionRepo.validarToken(token);
    }

    public void logout(String token) throws Exception {
        if (token == null) return;
        sessionRepo.deletar(token);
    }

    public List<User> listarTodos() throws Exception { return userRepo.buscarTodos(); }
    public User buscarPorId(Long id) throws Exception { return userRepo.buscarPorId(id); }

    public boolean atualizar(User u, String novaSenhaPlain) throws Exception {
        if (u == null || u.getId() == null) throw new IllegalArgumentException("Usuário inválido");
        if (novaSenhaPlain != null && !novaSenhaPlain.isBlank()) {
            String hash = BCrypt.hashpw(novaSenhaPlain, BCrypt.gensalt(12));
            u.setSenhaHash(hash);
        } else {
            User atual = userRepo.buscarPorId(u.getId());
            if (atual == null) throw new Exception("Usuário não encontrado");
            u.setSenhaHash(atual.getSenhaHash());
        }
        return userRepo.atualizar(u);
    }

    public boolean deletarPorId(Long id) throws Exception {
        sessionRepo.deletarPorUserId(id);
        return userRepo.deletarPorId(id);
    }

    public static class AuthResult {
        public final Long userId;
        public final String token;
        public final String nome;
        public final String email;
        public AuthResult(Long userId, String token, String nome, String email) {
            this.userId = userId; this.token = token; this.nome = nome; this.email = email;
        }
    }
}
