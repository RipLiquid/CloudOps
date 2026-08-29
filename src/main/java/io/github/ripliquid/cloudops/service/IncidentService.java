package io.github.ripliquid.cloudops.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github.ripliquid.cloudops.model.Incident;
import io.github.ripliquid.cloudops.repository.IncidentRepository;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(
            IncidentRepository incidentRepository
    ) {
        this.incidentRepository = incidentRepository;
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public Incident getIncidentById(String id) {
        return incidentRepository.findById(id);
    }

    public Incident createIncident(Incident incident) {

        incident.setId(
                UUID.randomUUID().toString()
        );

        return incidentRepository.save(incident);
    }

    public Incident updateIncident(
            String id,
            Incident updatedIncident
    ) {

        Incident existingIncident =
                incidentRepository.findById(id);

        if (existingIncident == null) {
            return null;
        }

        updatedIncident.setId(id);

        return incidentRepository.save(
                updatedIncident
        );
    }

    public boolean deleteIncident(String id) {
        return incidentRepository.deleteById(id);
    }
}