package io.github.ripliquid.cloudops.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ripliquid.cloudops.model.Incident;
import io.github.ripliquid.cloudops.model.IncidentStatus;
import io.github.ripliquid.cloudops.model.Severity;
import io.github.ripliquid.cloudops.repository.IncidentRepository;

class IncidentServiceTest {

    private IncidentRepository incidentRepository;
    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        incidentService = new IncidentService(incidentRepository);
    }

    @Test
    void shouldReturnAllIncidents() {

        Incident incident = new Incident(
                "test-id-1",
                "Authentication API Down",
                "Users are unable to log in.",
                Severity.CRITICAL,
                IncidentStatus.INVESTIGATING,
                "Daniyal"
        );

        when(incidentRepository.findAll())
                .thenReturn(List.of(incident));

        List<Incident> incidents =
                incidentService.getAllIncidents();

        assertEquals(1, incidents.size());
        assertEquals(
                "Authentication API Down",
                incidents.getFirst().getTitle()
        );
    }

    @Test
    void shouldReturnIncidentById() {

        Incident incident = new Incident(
                "test-id-1",
                "Authentication API Down",
                "Users are unable to log in.",
                Severity.CRITICAL,
                IncidentStatus.INVESTIGATING,
                "Daniyal"
        );

        when(incidentRepository.findById("test-id-1"))
                .thenReturn(incident);

        Incident result =
                incidentService.getIncidentById("test-id-1");

        assertNotNull(result);
        assertEquals("test-id-1", result.getId());
        assertEquals(
                Severity.CRITICAL,
                result.getSeverity()
        );
    }

    @Test
    void shouldReturnNullForMissingIncident() {

        when(incidentRepository.findById("missing-id"))
                .thenReturn(null);

        Incident result =
                incidentService.getIncidentById("missing-id");

        assertNull(result);
    }

    @Test
    void shouldCreateIncidentWithUuid() {

        Incident incident = new Incident(
                null,
                "Payment Service Failure",
                "Payments are unavailable.",
                Severity.HIGH,
                IncidentStatus.OPEN,
                "Daniyal"
        );

        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident created =
                incidentService.createIncident(incident);

        assertNotNull(created.getId());
        assertFalse(created.getId().isBlank());

        verify(incidentRepository)
                .save(incident);
    }

    @Test
    void shouldUpdateIncident() {

        String id = "test-id-1";

        Incident existing = new Incident(
                id,
                "Old Title",
                "Old Description",
                Severity.HIGH,
                IncidentStatus.OPEN,
                "Daniyal"
        );

        Incident updatedData = new Incident(
                null,
                "Updated Title",
                "Updated Description",
                Severity.LOW,
                IncidentStatus.RESOLVED,
                "Daniyal"
        );

        when(incidentRepository.findById(id))
                .thenReturn(existing);

        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident updated =
                incidentService.updateIncident(
                        id,
                        updatedData
                );

        assertNotNull(updated);
        assertEquals(id, updated.getId());
        assertEquals(
                "Updated Title",
                updated.getTitle()
        );
        assertEquals(
                IncidentStatus.RESOLVED,
                updated.getStatus()
        );
    }

    @Test
    void shouldReturnNullWhenUpdatingMissingIncident() {

        when(incidentRepository.findById("missing-id"))
                .thenReturn(null);

        Incident updatedData = new Incident(
                null,
                "Updated",
                "Updated Description",
                Severity.LOW,
                IncidentStatus.OPEN,
                "Daniyal"
        );

        Incident result =
                incidentService.updateIncident(
                        "missing-id",
                        updatedData
                );

        assertNull(result);

        verify(incidentRepository, never())
                .save(any());
    }

    @Test
    void shouldDeleteIncident() {

        when(incidentRepository.deleteById("test-id-1"))
                .thenReturn(true);

        boolean deleted =
                incidentService.deleteIncident("test-id-1");

        assertTrue(deleted);
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingIncident() {

        when(incidentRepository.deleteById("missing-id"))
                .thenReturn(false);

        boolean deleted =
                incidentService.deleteIncident("missing-id");

        assertFalse(deleted);
    }
}