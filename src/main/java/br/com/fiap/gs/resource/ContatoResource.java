package br.com.fiap.gs.resource;

import br.com.fiap.gs.model.Contato;
import br.com.fiap.gs.dto.ContatoDTO;
import br.com.fiap.gs.repository.ContatoRepository;
import br.com.fiap.gs.business.ContatoBusiness;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Path("/contato")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContatoResource {

    private final ContatoRepository repository = new ContatoRepository();
    private final ContatoBusiness business = new ContatoBusiness();

    @POST
    public Response enviarContato(ContatoDTO dto) {
        if (!business.validarCampos(dto)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Dados inválidos")).build();
        }

        try {
            Contato c = new Contato();
            c.setNome(dto.getNome());
            c.setEmail(dto.getEmail());
            c.setMensagem(dto.getMensagem());
            c.setId_usuario(dto.getId_usuario()); // pode ser null
            c.setDataSend(LocalDateTime.now());
            c.setStatus_contact(0);

            repository.salvar(c);
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Contato registrado com sucesso!")).build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity(Map.of("error", "Erro ao salvar o contato: " + e.getMessage())).build();
        }
    }

    @GET
    public Response listarTodos() {
        try {
            List<Contato> lista = repository.buscarTodas();
            return Response.ok(lista).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", "Erro ao buscar contatos.")).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") long id) {
        try {
            Contato c = repository.buscarPorId(id);
            if (c == null)
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Contato não encontrado.")).build();
            return Response.ok(c).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", "Erro ao buscar contato.")).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response atualizarContato(@PathParam("id") long id, Contato c) {
        try {
            c.setId_contact(id);
            boolean atualizado = repository.update(c);
            if (atualizado)
                return Response.ok(Map.of("message", "Contato atualizado com sucesso!")).build();
            else
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Contato não encontrado.")).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", "Erro ao atualizar contato.")).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deletarContato(@PathParam("id") long id) {
        try {
            boolean deletado = repository.deletarPorId(id);
            if (deletado)
                return Response.noContent().build();
            else
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Contato não encontrado.")).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", "Erro ao deletar contato.")).build();
        }
    }
}
