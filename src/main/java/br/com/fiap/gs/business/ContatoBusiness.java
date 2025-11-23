package br.com.fiap.gs.business;

import br.com.fiap.gs.model.ContatoDTO;

public class ContatoBusiness {

    public boolean validarCampos(ContatoDTO dto) {
        return dto != null &&
                dto.getNome() != null && !dto.getNome().isBlank() &&
                dto.getEmail() != null && dto.getEmail().contains("@") &&
                dto.getMensagem() != null && !dto.getMensagem().isBlank();
    }
}
