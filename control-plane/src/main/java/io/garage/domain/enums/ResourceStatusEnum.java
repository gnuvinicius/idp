package io.garage.domain.enums;

public enum ResourceStatusEnum {
    PENDING,        // Solicitado pela CLI, aguardando execução
    PROVISIONING,   // Terraform em execução
    READY,          // Recurso criado com sucesso
    FAILED,         // Falha no provisionamento
    DESTROYING,     // Exclusão em andamento
    DESTROYED       // Recurso removido
}
