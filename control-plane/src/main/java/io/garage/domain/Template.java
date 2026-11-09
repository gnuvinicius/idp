package io.garage.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Modela um modelo-base de software ("Golden Path") catalogado na plataforma.
 * <p>
 * Define arquétipos de projetos pré-padronizados com pipelines de CI/CD, dependências
 * e boas práticas de arquitetura recomendadas pela equipe de engenharia de plataforma
 * (ex.: "Microsserviço Quarkus com gRPC", "CLI em Go").
 * </p>
 * <p>
 * Quando um usuário cria um novo {@link Project}, o IDP utiliza o repositório deste template
 * como fonte para clonar o código, customizar os parâmetros iniciais e criar o repositório
 * final na organização do GitHub.
 * </p>
 *
 * @see Project
 */
@Entity
@Table(name = "templates")
public class Template extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String name;

    public String description;

    @Column(name = "git_repository_url", nullable = false)
    public String gitRepositoryUrl;
}
