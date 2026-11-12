package io.garage.infra.entity;

import java.time.Instant;

import io.garage.catalog.entity.Project;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

/**
 * Representa um ambiente de implantação isolado associado a um {@link Project}.
 * <p>
 * O ambiente define o escopo lógico e físico de execução das aplicações
 * (ex.: {@code development}, {@code staging}, {@code production}), encapsulando
 * o contexto da nuvem de destino (conta e região AWS).
 * Todos os recursos de infraestrutura provisionados pertencem a um ambiente específico.
 * </p>
 *
 * @see Project
 * @see InfrastructureResource
 */
@Entity
@Table(name = "environments")
public class Environment extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    public Project project;

    @Column(name = "aws_region", nullable = false)
    public String awsRegion;

    @Column(name = "aws_account_id", nullable = false)
    public String awsAccountId;

    public Instant createdAt = Instant.now();
}
