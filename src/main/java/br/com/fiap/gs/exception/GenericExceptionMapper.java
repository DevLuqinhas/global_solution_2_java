package br.com.fiap.gs.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception ex) {
        ex.printStackTrace(); // log server-side
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(java.util.Map.of("error","Erro interno. Tente novamente mais tarde."))
                .build();
    }
}
