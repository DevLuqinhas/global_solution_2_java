package br.com.fiap.gs.resource;

import br.com.fiap.gs.business.UserBusiness;
import br.com.fiap.gs.model.User;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserBusiness business = new UserBusiness();

    // list all (protected)
    @GET
    public Response listar(@HeaderParam("Authorization") String authHeader) {
        try {
            if (!validToken(authHeader)) return Response.status(Response.Status.UNAUTHORIZED).entity("Token inválido").build();
            List<User> lista = business.listarTodos();
            return Response.ok(lista).build();
        } catch (Exception e) {
            return Response.serverError().entity("Erro ao buscar usuários").build();
        }
    }

    // get by id (protected)
    @GET
    @Path("/{id}")
    public Response buscar(@HeaderParam("Authorization") String authHeader, @PathParam("id") Long id) {
        try {
            if (!validToken(authHeader)) return Response.status(Response.Status.UNAUTHORIZED).entity("Token inválido").build();
            User u = business.buscarPorId(id);
            if (u == null) return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
            return Response.ok(u).build();
        } catch (Exception e) {
            return Response.serverError().entity("Erro ao buscar usuário").build();
        }
    }

    // update (protected) - body includes optional novaSenhaPlain in "senha" field if changing
    @PUT
    @Path("/{id}")
    public Response atualizar(@HeaderParam("Authorization") String authHeader, @PathParam("id") Long id, User payload) {
        try {
            if (!validToken(authHeader)) return Response.status(Response.Status.UNAUTHORIZED).entity("Token inválido").build();
            payload.setId(id);
            String novaSenha = payload.getSenhaHash(); // the frontend should send `senha` in this field if changing
            // if frontend wants better API, create DTO; here we keep simple
            boolean ok = business.atualizar(payload, novaSenha);
            if (ok) return Response.ok("Atualizado").build();
            return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
        } catch (Exception e) {
            return Response.serverError().entity("Erro ao atualizar usuário").build();
        }
    }

    // delete (protected)
    @DELETE
    @Path("/{id}")
    public Response deletar(@HeaderParam("Authorization") String authHeader, @PathParam("id") Long id) {
        try {
            if (!validToken(authHeader)) return Response.status(Response.Status.UNAUTHORIZED).entity("Token inválido").build();
            boolean ok = business.deletarPorId(id);
            if (ok) return Response.noContent().build();
            return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
        } catch (Exception e) {
            return Response.serverError().entity("Erro ao deletar usuário").build();
        }
    }

    // helper: valida token e devolve boolean
    private boolean validToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring("Bearer ".length());
        try {
            Long uid = business.validarToken(token);
            return uid != null;
        } catch (Exception e) {
            return false;
        }
    }
}
