package io.garage.domain;

import java.time.Instant;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    public User user;

    @ManyToOne
    public Template templateUsed;

    public Instant createdAt = Instant.now();
}
