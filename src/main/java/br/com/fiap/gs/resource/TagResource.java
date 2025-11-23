package br.com.fiap.gs.resource;

import br.com.fiap.gs.business.TagBusiness;
import br.com.fiap.gs.business.UserBusiness;
import br.com.fiap.gs.model.Tag;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagResource {

    private final TagBusiness business = new TagBusiness();
    private final UserBusiness userBusiness = new UserBusiness();

    @GET
    public Response listar(@HeaderParam("Authorization") String authHeader) {
        try {
            Long userId = extractUserId(authHeader);
            return Response.ok(business.listar(userId)).build();
        } catch (WebApplicationException wae) {
            return wae.getResponse();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    public Response criar(@HeaderParam("Authorization") String authHeader, Map<String, String> body) {
        try {
            Long userId = extractUserId(authHeader);
            String nome = body.get("nome");
            Tag tag = business.encontrarOuCriar(nome, userId);
            return Response.status(Response.Status.CREATED).entity(tag).build();
        } catch (WebApplicationException wae) {
            return wae.getResponse();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    // helper para validar e extrair userId
    private Long extractUserId(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Token ausente")).build());
        }
        String token = authHeader.substring("Bearer ".length());
        Long userId = userBusiness.validarToken(token);
        if (userId == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Token inválido")).build());
        }
        return userId;
    }
}
