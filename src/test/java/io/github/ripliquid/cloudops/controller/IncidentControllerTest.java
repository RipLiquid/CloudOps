package io.github.ripliquid.cloudops.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.ripliquid.cloudops.model.Incident;
import io.github.ripliquid.cloudops.model.IncidentStatus;
import io.github.ripliquid.cloudops.model.Severity;
import io.github.ripliquid.cloudops.service.IncidentService;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService incidentService;

    @Test
    void shouldReturnAllIncidents() throws Exception {

        Incident incident = new Incident(
                1L,
                "Authentication API Down",
                "Users are unable to log in.",
                Severity.CRITICAL,
                IncidentStatus.INVESTIGATING,
                "Daniyal"
        );

        when(incidentService.getAllIncidents())
                .thenReturn(List.of(incident));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Authentication API Down")
                );
    }

    @Test
    void shouldReturnIncidentById() throws Exception {

        Incident incident = new Incident(
                1L,
                "Authentication API Down",
                "Users are unable to log in.",
                Severity.CRITICAL,
                IncidentStatus.INVESTIGATING,
                "Daniyal"
        );

        when(incidentService.getIncidentById(1L))
                .thenReturn(incident);

        mockMvc.perform(get("/api/incidents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.severity")
                                .value("CRITICAL")
                );
    }

    @Test
    void shouldReturn404ForMissingIncident() throws Exception {

        when(incidentService.getIncidentById(999L))
                .thenReturn(null);

        mockMvc.perform(get("/api/incidents/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateIncident() throws Exception {

        Incident created = new Incident(
                3L,
                "Storage Failure",
                "Uploads are unavailable.",
                Severity.HIGH,
                IncidentStatus.OPEN,
                "Daniyal"
        );

        when(incidentService.createIncident(any(Incident.class)))
                .thenReturn(created);

        String body = """
                {
                    "title": "Storage Failure",
                    "description": "Uploads are unavailable.",
                    "severity": "HIGH",
                    "status": "OPEN",
                    "owner": "Daniyal"
                }
                """;

        mockMvc.perform(
                        post("/api/incidents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(
                        jsonPath("$.title")
                                .value("Storage Failure")
                );
    }

    @Test
    void shouldRejectInvalidIncident() throws Exception {

        String body = """
                {
                    "title": "",
                    "description": "",
                    "severity": "HIGH",
                    "status": "OPEN",
                    "owner": ""
                }
                """;

        mockMvc.perform(
                        post("/api/incidents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteIncident() throws Exception {

        when(incidentService.deleteIncident(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/incidents/1"))
                .andExpect(status().isNoContent());
    }
}