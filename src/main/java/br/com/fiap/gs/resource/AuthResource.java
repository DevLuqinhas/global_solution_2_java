package br.com.fiap.gs.resource;

import br.com.fiap.gs.business.UserBusiness;
import br.com.fiap.gs.business.UserBusiness.AuthResult;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UserBusiness business = new UserBusiness();

    public static class RegisterDTO { public String nome; public String email; public String senha; }
    public static class LoginDTO { public String email; public String senha; }

    @POST
    @Path("/register")
    public Response register(RegisterDTO dto) {
        if (dto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of("error", "Payload ausente")).build();
        }
        String nome = dto.nome != null ? dto.nome.trim() : "";
        String email = dto.email != null ? dto.email.trim().toLowerCase() : null;
        String senha = dto.senha != null ? dto.senha : null;

        if (email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of("error", "Email é obrigatório")).build();
        }
        if (senha == null || senha.length() < 6) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of("error", "Senha é obrigatória (mínimo 6 caracteres)")).build();
        }

        try {
            AuthResult res = business.registrarEAutenticar(nome, email, senha, 24);
            return Response.status(Response.Status.CREATED).entity(java.util.Map.of(
                    "userId", res.userId,
                    "token", res.token,
                    "nome", res.nome,
                    "email", res.email
            )).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            // se o e.getMessage() for sensível, mantenha uma mensagem amigável
            return Response.status(Response.Status.CONFLICT).entity(java.util.Map.of("error", e.getMessage())).build();
        }
    }


    @POST
    @Path("/login")
    public Response login(LoginDTO dto) {
        if (dto == null || dto.email == null || dto.senha == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of("error", "Email e senha são obrigatórios")).build();
        }
        try {
            AuthResult res = business.loginEAutenticar(dto.email.toLowerCase(), dto.senha, 24);
            return Response.ok(java.util.Map.of(
                    "userId", res.userId,
                    "token", res.token,
                    "nome", res.nome,
                    "email", res.email
            )).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(java.util.Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/register-debug")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerDebug(String raw) {
        System.out.println("RAW JSON: " + raw);
        return Response.ok(java.util.Map.of("received", true)).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Map.of("error","Token ausente")).build();
        }
        String token = authHeader.substring("Bearer ".length());
        try {
            business.logout(token);
            return Response.ok(java.util.Map.of("message","Logout realizado")).build();
        } catch (Exception e) {
            return Response.serverError().entity(java.util.Map.of("error","Erro ao fazer logout")).build();
        }
    }
}
