package io.github.ripliquid.cloudops.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.ripliquid.cloudops.model.Incident;
import io.github.ripliquid.cloudops.service.IncidentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(
            IncidentService incidentService
    ) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<Incident> getIncidents() {
        return incidentService.getAllIncidents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Incident> getIncidentById(
            @PathVariable Long id
    ) {

        Incident incident =
                incidentService.getIncidentById(id);

        if (incident == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(incident);
    }

    @PostMapping
    public ResponseEntity<Incident> createIncident(
            @Valid @RequestBody Incident incident
    ) {

        Incident createdIncident =
                incidentService.createIncident(incident);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/incidents/"
                                        + createdIncident.getId()
                        )
                )
                .body(createdIncident);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Incident> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody Incident incident
    ) {

        Incident updatedIncident =
                incidentService.updateIncident(
                        id,
                        incident
                );

        if (updatedIncident == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedIncident);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(
            @PathVariable Long id
    ) {

        boolean deleted =
                incidentService.deleteIncident(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}