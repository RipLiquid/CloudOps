package io.github.ripliquid.cloudops.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Incident {

    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Severity severity;

    @NotNull
    private IncidentStatus status;

    @NotBlank
    private String owner;

    public Incident() {
    }

    public Incident(
            String id,
            String title,
            String description,
            Severity severity,
            IncidentStatus status,
            String owner
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}