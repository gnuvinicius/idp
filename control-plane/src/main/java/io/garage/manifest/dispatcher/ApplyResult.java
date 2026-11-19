package io.garage.manifest.dispatcher;

public record ApplyResult(
        ManifestyTypeEnum type,
        String name,
        ApplyStatusEnum status,
        String message
) {
    public static ApplyResult created(ManifestyTypeEnum type, String name) {
        return new ApplyResult(type, name, ApplyStatusEnum.CREATED, "Resource created successfully");
    }

    public static ApplyResult updated(ManifestyTypeEnum type, String name) {
        return new ApplyResult(type, name, ApplyStatusEnum.UPDATED, "Resource updated successfully");
    }

    public static ApplyResult unchanged(ManifestyTypeEnum type, String name) {
        return new ApplyResult(type, name, ApplyStatusEnum.UNCHANGED, "No changes detected");
    }

    public static ApplyResult error(ManifestyTypeEnum type, String name, String message) {
        return new ApplyResult(type, name, ApplyStatusEnum.ERROR, message);
    }
}
