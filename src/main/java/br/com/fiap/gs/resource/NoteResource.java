package br.com.fiap.gs.resource;

import br.com.fiap.gs.business.NoteBusiness;
import br.com.fiap.gs.business.UserBusiness;
import br.com.fiap.gs.dto.NoteWithTagsDTO;
import br.com.fiap.gs.model.Note;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoteResource {

    private final NoteBusiness business = new NoteBusiness();
    private final UserBusiness userBusiness = new UserBusiness();

    public static class CreateNoteDTO {
        public String title;
        public String content;
        public String[] tags;
    }

    // Criar nota
    @POST
    public Response create(@HeaderParam("Authorization") String authHeader, CreateNoteDTO dto) {
        try {
            Long userId = extractUserId(authHeader);

            Long id = business.criarNota(
                    dto.title,
                    dto.content,
                    dto.tags != null ? Arrays.asList(dto.tags) : List.of(),
                    userId
            );

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("id", id))
                    .build();

        } catch (WebApplicationException wae) {
            // devolve o response já preparado pela helper (401 com JSON)
            return wae.getResponse();
        } catch (Exception e) {
            e.printStackTrace(); // log no servidor para debugging
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // LIST agora retorna NoteWithTagsDTO (tags como lista de nomes)
    @GET
    public Response list(@HeaderParam("Authorization") String authHeader) {
        try {
            Long userId = extractUserId(authHeader);
            List<NoteWithTagsDTO> notes = business.listarPorUsuarioComTags(userId);
            return Response.ok(notes).build();
        } catch (WebApplicationException wae) {
            return wae.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response get(@HeaderParam("Authorization") String authHeader, @PathParam("id") Long id) {
        try {
            Long userId = extractUserId(authHeader);
            NoteWithTagsDTO dto = business.buscarPorIdComTags(id);

            if (dto == null || !dto.userId.equals(userId)) {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Nota não encontrada")).build();
            }

            return Response.ok(dto).build();

        } catch (WebApplicationException wae) {
            return wae.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", "Erro ao buscar nota")).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("id") Long id,
            Note payload
    ) {
        try {
            Long userId = extractUserId(authHeader);

            payload.setId(id);
            payload.setUserId(userId);

            boolean ok = business.atualizar(payload, userId);

            if (!ok) {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Nota não encontrada")).build();
            }

            return Response.ok(Map.of("message","Atualizado")).build();

        } catch (WebApplicationException wae) {
            return wae.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", "Erro ao atualizar nota")).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@HeaderParam("Authorization") String authHeader, @PathParam("id") Long id) {
        try {
            Long userId = extractUserId(authHeader);
            boolean ok = business.deletar(id, userId);

            if (!ok) return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Nota não encontrada")).build();
            return Response.noContent().build();

        } catch (WebApplicationException wae) {
            return wae.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", "Erro ao deletar nota")).build();
        }
    }

    // helper: agora retorna JSON claro quando token ausente/inválido
    private Long extractUserId(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Token ausente")).build());
        }

        String token = authHeader.substring("Bearer ".length());
        Long id = userBusiness.validarToken(token);

        if (id == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Token inválido")).build());
        }

        return id;
    }
}
