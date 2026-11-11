package br.com.carloslonghi.eletrolonghi.entity.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Administrador"),
    GERENTE("Gerente"),
    ATENDENTE("Atendente"),
    TECNICO("Técnico"),
    PENDENTE("Pendente");

    private final String description;

    Role(String description) {
        this.description = description;
    }
}
