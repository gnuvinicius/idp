package io.garage.catalog.entity;

import java.time.Instant;

import io.garage.auth.User;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class Project extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String name;

    public String description;

    @Column(name = "repository_url", nullable = false)
    public String repositoryUrl;

    @Column(name = "default_branch")
    public String defaultBranch = "main";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ProjectStatusEnum status = ProjectStatusEnum.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    public User user;

    @ManyToOne
    public Template templateUsed;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    public Project() {
    }

    public Project(String name,
                   String description,
                   String repositoryUrl,
                   Template template,
                   User user) {
        this.name = name;
        this.description = description;
        this.repositoryUrl = repositoryUrl;
        this.templateUsed = template;
        this.user = user;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Project findByName(String name) {
        return find("name", name).firstResult();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

}
