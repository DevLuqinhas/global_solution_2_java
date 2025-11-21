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
        try {
            AuthResult res = business.registrarEAutenticar(dto.nome, dto.email.toLowerCase(), dto.senha, 24);
            return Response.status(Response.Status.CREATED).entity(java.util.Map.of(
                    "userId", res.userId,
                    "token", res.token,
                    "nome", res.nome,
                    "email", res.email
            )).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(java.util.Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginDTO dto) {
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
