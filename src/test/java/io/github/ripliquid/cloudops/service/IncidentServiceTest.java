package io.github.ripliquid.cloudops.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.ripliquid.cloudops.model.Incident;
import io.github.ripliquid.cloudops.model.IncidentStatus;
import io.github.ripliquid.cloudops.model.Severity;

class IncidentServiceTest {

    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService();
    }

    @Test
    void shouldReturnAllIncidents() {

        List<Incident> incidents =
                incidentService.getAllIncidents();

        assertEquals(2, incidents.size());
    }

    @Test
    void shouldReturnIncidentById() {

        Incident incident =
                incidentService.getIncidentById(1L);

        assertNotNull(incident);
        assertEquals(
                "Authentication API Down",
                incident.getTitle()
        );
        assertEquals(
                Severity.CRITICAL,
                incident.getSeverity()
        );
    }

    @Test
    void shouldReturnNullForMissingIncident() {

        Incident incident =
                incidentService.getIncidentById(999L);

        assertNull(incident);
    }

    @Test
    void shouldCreateIncident() {

        Incident newIncident = new Incident(
                null,
                "Payment Service Failure",
                "Payments are unavailable.",
                Severity.HIGH,
                IncidentStatus.OPEN,
                "Daniyal"
        );

        Incident created =
                incidentService.createIncident(newIncident);

        assertNotNull(created.getId());
        assertEquals(3L, created.getId());
        assertEquals(
                "Payment Service Failure",
                created.getTitle()
        );

        assertEquals(
                3,
                incidentService.getAllIncidents().size()
        );
    }

    @Test
    void shouldUpdateIncident() {

        Incident updatedData = new Incident(
                null,
                "Authentication API Restored",
                "Login functionality has been restored.",
                Severity.LOW,
                IncidentStatus.RESOLVED,
                "Daniyal"
        );

        Incident updated =
                incidentService.updateIncident(
                        1L,
                        updatedData
                );

        assertNotNull(updated);

        assertEquals(
                "Authentication API Restored",
                updated.getTitle()
        );

        assertEquals(
                Severity.LOW,
                updated.getSeverity()
        );

        assertEquals(
                IncidentStatus.RESOLVED,
                updated.getStatus()
        );
    }

    @Test
    void shouldReturnNullWhenUpdatingMissingIncident() {

        Incident updatedData = new Incident(
                null,
                "Missing Incident",
                "This incident does not exist.",
                Severity.LOW,
                IncidentStatus.OPEN,
                "Daniyal"
        );

        Incident result =
                incidentService.updateIncident(
                        999L,
                        updatedData
                );

        assertNull(result);
    }

    @Test
    void shouldDeleteIncident() {

        boolean deleted =
                incidentService.deleteIncident(1L);

        assertTrue(deleted);

        assertNull(
                incidentService.getIncidentById(1L)
        );

        assertEquals(
                1,
                incidentService.getAllIncidents().size()
        );
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingIncident() {

        boolean deleted =
                incidentService.deleteIncident(999L);

        assertFalse(deleted);
    }
}