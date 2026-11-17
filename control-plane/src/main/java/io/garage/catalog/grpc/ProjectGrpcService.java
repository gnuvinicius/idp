package io.garage.catalog.grpc;

import io.quarkus.grpc.GrpcService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;

@GrpcService
@Authenticated
public class ProjectGrpcService implements ProjectService {

    @Override
    public Uni<ProjectResponse> getProject(GetProjectRequest request) {
        return null;
    }

    @Override
    public Uni<ListProjectsResponse> listProjects(ListProjectsRequest request) {
        return null;
    }

    @Override
    public Uni<DeleteProjectResponse> deleteProject(DeleteProjectRequest request) {
        return null;
    }
}
