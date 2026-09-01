package io.github.ripliquid.cloudops.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.ripliquid.cloudops.config.SecurityConfig;
import io.github.ripliquid.cloudops.model.Incident;
import io.github.ripliquid.cloudops.model.IncidentStatus;
import io.github.ripliquid.cloudops.model.Severity;
import io.github.ripliquid.cloudops.service.IncidentService;

@WebMvcTest(IncidentController.class)
@Import(SecurityConfig.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService incidentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private RequestPostProcessor demoUser() {
        return jwt().authorities(
                new SimpleGrantedAuthority(
                        "GROUP_DemoUsers"
                )
        );
    }

    private RequestPostProcessor adminUser() {
        return jwt().authorities(
                new SimpleGrantedAuthority(
                        "GROUP_Admins"
                )
        );
    }

    private static final String INCIDENT_JSON = """
            {
                "title": "Storage Failure",
                "description": "Uploads are unavailable.",
                "severity": "HIGH",
                "status": "OPEN",
                "owner": "Daniyal"
            }
            """;

    private static final String UPDATED_INCIDENT_JSON = """
            {
                "title": "Storage Failure Updated",
                "description": "Storage is recovering.",
                "severity": "MEDIUM",
                "status": "INVESTIGATING",
                "owner": "Daniyal"
            }
            """;

    @Test
    void anonymousUserCannotViewIncidents()
            throws Exception {

        mockMvc.perform(
                get("/api/incidents")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    void demoUserCanViewIncidents()
            throws Exception {

        Incident incident = new Incident(
                "test-id-1",
                "Authentication API Down",
                "Users are unable to log in.",
                Severity.CRITICAL,
                IncidentStatus.INVESTIGATING,
                "Daniyal"
        );

        when(
                incidentService.getAllIncidents()
        ).thenReturn(
                List.of(incident)
        );

        mockMvc.perform(
                get("/api/incidents")
                        .with(demoUser())
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$[0].id")
                        .value("test-id-1")
        );
    }

    @Test
    void demoUserCanViewIncidentById()
            throws Exception {

        Incident incident = new Incident(
                "test-id-1",
                "Authentication API Down",
                "Users are unable to log in.",
                Severity.CRITICAL,
                IncidentStatus.INVESTIGATING,
                "Daniyal"
        );

        when(
                incidentService.getIncidentById(
                        "test-id-1"
                )
        ).thenReturn(incident);

        mockMvc.perform(
                get("/api/incidents/test-id-1")
                        .with(demoUser())
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.id")
                        .value("test-id-1")
        );
    }

    @Test
    void demoUserCanCreateIncident()
            throws Exception {

        Incident created = new Incident(
                "generated-id",
                "Storage Failure",
                "Uploads are unavailable.",
                Severity.HIGH,
                IncidentStatus.OPEN,
                "Daniyal"
        );

        when(
                incidentService.createIncident(
                        any(Incident.class)
                )
        ).thenReturn(created);

        mockMvc.perform(
                post("/api/incidents")
                        .with(demoUser())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(INCIDENT_JSON)
        )
        .andExpect(
                status().isCreated()
        )
        .andExpect(
                jsonPath("$.id")
                        .value("generated-id")
        );
    }

    @Test
    void demoUserCannotEditIncident()
            throws Exception {

        mockMvc.perform(
                put("/api/incidents/test-id-1")
                        .with(demoUser())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                UPDATED_INCIDENT_JSON
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void demoUserCannotDeleteIncident()
            throws Exception {

        mockMvc.perform(
                delete("/api/incidents/test-id-1")
                        .with(demoUser())
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void adminUserCanEditIncident()
            throws Exception {

        Incident updated = new Incident(
                "test-id-1",
                "Storage Failure Updated",
                "Storage is recovering.",
                Severity.MEDIUM,
                IncidentStatus.INVESTIGATING,
                "Daniyal"
        );

        when(
                incidentService.updateIncident(
                        any(String.class),
                        any(Incident.class)
                )
        ).thenReturn(updated);

        mockMvc.perform(
                put("/api/incidents/test-id-1")
                        .with(adminUser())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                UPDATED_INCIDENT_JSON
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.id")
                        .value("test-id-1")
        );
    }

    @Test
    void adminUserCanDeleteIncident()
            throws Exception {

        when(
                incidentService.deleteIncident(
                        "test-id-1"
                )
        ).thenReturn(true);

        mockMvc.perform(
                delete("/api/incidents/test-id-1")
                        .with(adminUser())
        )
        .andExpect(
                status().isNoContent()
        );
    }

    @Test
    void demoUserGets404ForMissingIncident()
            throws Exception {

        when(
                incidentService.getIncidentById(
                        "missing-id"
                )
        ).thenReturn(null);

        mockMvc.perform(
                get("/api/incidents/missing-id")
                        .with(demoUser())
        )
        .andExpect(
                status().isNotFound()
        );
    }

    @Test
    void demoUserCannotCreateInvalidIncident()
            throws Exception {

        String invalidIncident = """
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
                        .with(demoUser())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                invalidIncident
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }
}