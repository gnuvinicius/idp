package io.garage.orchestration.entity;

import io.garage.auth.User;
import io.garage.infra.entity.InfrastructureResource;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Registra a execução de um trabalho assíncrono de provisionamento, modificação ou destruição
 * de um {@link InfrastructureResource}.
 * <p>
 * Como operações de infraestrutura na nuvem costumam demandar de minutos a horas, esta entidade
 * atua como o registro histórico e de auditoria da execução. Armazena o ciclo de vida do job
 * (fila, execução, término) e retém os logs de saída gerados pelo motor de IaC para
 * transmissão em tempo real via streaming gRPC ou consulta posterior via CLI.
 * </p>
 *
 * @see InfrastructureResource
 * @see User
 * @see TaskActionEnum
 * @see TaskStatusEnum
 */
@Entity
@Table(name = "provisioning_tasks")
public class ProvisioningTask extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id")
    public InfrastructureResource resource;

    @ManyToOne(optional = false)
    @JoinColumn(name = "triggered_by_id")
    public User triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskActionEnum action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskStatusEnum status = TaskStatusEnum.QUEUED;

    @Column(columnDefinition = "TEXT")
    public String executionLogs; // Buffer de saída do Terraform para streaming na CLI

    public Instant startedAt;
    public Instant finishedAt;

}
