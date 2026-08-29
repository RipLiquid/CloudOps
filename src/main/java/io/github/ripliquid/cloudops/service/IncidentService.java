package io.github.ripliquid.cloudops.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.ripliquid.cloudops.model.Incident;
import io.github.ripliquid.cloudops.model.IncidentStatus;
import io.github.ripliquid.cloudops.model.Severity;

@Service
public class IncidentService {

    private final List<Incident> incidents = new ArrayList<>();

    private long nextId = 1;

    public IncidentService() {

        incidents.add(new Incident(
                nextId++,
                "Authentication API Down",
                "Users are unable to log in.",
                Severity.CRITICAL,
                IncidentStatus.INVESTIGATING,
                "Daniyal"
        ));

        incidents.add(new Incident(
                nextId++,
                "Database Latency",
                "Database response times are elevated.",
                Severity.HIGH,
                IncidentStatus.OPEN,
                "Unassigned"
        ));
    }

    public List<Incident> getAllIncidents() {
        return incidents;
    }

    public Incident getIncidentById(Long id) {
        return incidents.stream()
                .filter(incident -> incident.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Incident createIncident(Incident incident) {

        incident.setId(nextId++);

        incidents.add(incident);

        return incident;
    }

    public Incident updateIncident(
            Long id,
            Incident updatedIncident
    ) {

        Incident existingIncident = getIncidentById(id);

        if (existingIncident == null) {
            return null;
        }

        existingIncident.setTitle(
                updatedIncident.getTitle()
        );

        existingIncident.setDescription(
                updatedIncident.getDescription()
        );

        existingIncident.setSeverity(
                updatedIncident.getSeverity()
        );

        existingIncident.setStatus(
                updatedIncident.getStatus()
        );

        existingIncident.setOwner(
                updatedIncident.getOwner()
        );

        return existingIncident;
    }

    public boolean deleteIncident(Long id) {

        return incidents.removeIf(
                incident -> incident.getId().equals(id)
        );
    }
}