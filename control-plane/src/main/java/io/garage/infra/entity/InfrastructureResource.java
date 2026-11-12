package io.garage.infra.entity;

import io.garage.orchestration.entity.ProvisioningTask;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

/**
 * Representa uma unidade de infraestrutura gerenciada pelo IDP dentro de um {@link Environment}.
 * <p>
 * Esta classe adota o padrão de <b>Recurso Parametrizado</b>. Em vez de modelar entidades
 * separadas para cada serviço de nuvem (RDS, S3, ECS, EC2), as especificidades de cada recurso
 * são encapsuladas em estruturas JSON ({@link #configurationJson} e {@link #outputsJson}).
 * </p>
 * <p>
 * Essa abordagem permite que o backend atue como uma ponte agnóstica entre as intenções da CLI
 * e os motores de execução de IaC (Terraform, OpenTofu ou Crossplane).
 * </p>
 *
 * @see Environment
 * @see ProvisioningTask
 * @see ResourceTypeEnum
 * @see ResourceStatusEnum
 */
@Entity
@Table(name = "infrastructure_resources")
public class InfrastructureResource extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id")
    public Environment environment;

    @Column(nullable = false)
    public String name; // ex: "payment-db", "assets-bucket"

    /**
     * Tipo do recurso cloud gerenciado (ex.: RDS, S3, ECS, EC2).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ResourceTypeEnum type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ResourceStatusEnum status = ResourceStatusEnum.PENDING;

    /**
     * Parâmetros de entrada em formato JSON enviados pela CLI para configurar o módulo IaC.
     * <p>
     * Exemplo (RDS): {@code {"instanceClass": "db.t4g.micro", "storageGb": 20, "engine": "postgres"}}
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    public String configurationJson;

    /**
     * Metadados e saídas exportadas após a conclusão do provisionamento pelo motor de IaC.
     * <p>
     * Exemplo (RDS): {@code {"endpoint": "orders-db.c123.sa-east-1.rds.amazonaws.com", "port": 5432}}
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    public String outputsJson;

}
