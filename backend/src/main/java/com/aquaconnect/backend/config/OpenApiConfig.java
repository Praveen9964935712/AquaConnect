package com.aquaconnect.backend.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI aquaConnectOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("AquaConnect API")
                        .description("REST API for AquaConnect's water incident workflow. Most endpoints require a JWT bearer token and role authorization. The IVR gateway is a separate provider trust boundary using X-IVR-Trust and X-IVR-Caller-Id.")
                        .version("1.0.0"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")));
    }

    @Bean
    OpenApiCustomizer aquaConnectEndpointDocumentation() {
        Map<String, EndpointDoc> documentation = endpointDocumentation();
        return openApi -> openApi.getPaths().forEach((path, pathItem) ->
                pathItem.readOperationsMap().forEach((method, operation) -> {
                    EndpointDoc doc = documentation.get(method.name() + " " + path);
                    if (doc == null) {
                        return;
                    }
                    operation.setTags(List.of(doc.tag()));
                    operation.setSummary(doc.summary());
                    operation.setDescription(doc.description() + "\n\nAccess: " + doc.access()
                            + "\n\nCommon errors: 400 for invalid input or state, 401 when authentication/trust is missing or invalid, and 403 when the caller lacks permission or ownership. A resource lookup may return 404.");
                    operation.setSecurity(doc.jwtRequired()
                            ? List.of(new SecurityRequirement().addList("bearerAuth"))
                            : List.of());
                    if (path.startsWith("/api/ivr/gateway/")) {
                        operation.getParameters().stream()
                                .filter(parameter -> "X-IVR-Trust".equals(parameter.getName()))
                                .forEach(parameter -> parameter.setDescription("Provider trust token. Supply the configured value through the trusted provider integration; never expose it in client code or documentation."));
                        operation.getParameters().stream()
                                .filter(parameter -> "X-IVR-Caller-Id".equals(parameter.getName()))
                                .forEach(parameter -> parameter.setDescription("Caller identity established by the trusted IVR provider. Session ownership is bound to this header; a request body cannot override it."));
                    }
                }));
    }

    private Map<String, EndpointDoc> endpointDocumentation() {
        Map<String, EndpointDoc> docs = new LinkedHashMap<>();
        add(docs, "POST", "/api/auth/register", "Authentication", "Register a citizen account", "Creates an account with the CITIZEN role. Privileged roles are assigned only through the protected admin API.", "Public", false);
        add(docs, "POST", "/api/auth/login", "Authentication", "Authenticate and issue a JWT", "Verifies email and password and returns a bearer token, user ID, email, and assigned roles.", "Public", false);
        add(docs, "POST", "/api/incidents", "Citizen", "Report an incident", "Creates an incident for the authenticated citizen. Initial status and priority are assigned by the backend; clients cannot set lifecycle status.", "CITIZEN; resource owner is taken from the JWT", true);
        add(docs, "GET", "/api/incidents", "Citizen", "List the current citizen's incidents", "Returns incidents owned by the authenticated citizen.", "CITIZEN; resource owner is taken from the JWT", true);
        add(docs, "GET", "/api/incidents/{id}", "Citizen", "Get an owned incident", "Returns one incident only when it belongs to the authenticated citizen.", "CITIZEN; ownership checked by the backend", true);
        add(docs, "POST", "/api/incidents/{id}/resolution-confirmation", "Citizen", "Confirm or reopen a resolution", "Submits the citizen's resolution decision for an owned incident. Operational users, not citizens, determine the RESOLVED transition.", "CITIZEN; incident ownership checked by the backend", true);
        add(docs, "GET", "/api/work-orders", "Work Orders", "List work orders for the current actor", "Returns work orders visible to the authenticated actor according to role and assignment.", "OPERATOR, OPERATIONS_MANAGER, FIELD_ENGINEER, or ADMIN; visibility is actor-scoped", true);
        add(docs, "POST", "/api/work-orders", "Work Orders", "Create a work order", "Creates a work order for an incident.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "GET", "/api/work-orders/{id}", "Work Orders", "Get an accessible work order", "Returns a work order only when the authenticated actor is authorized for it.", "OPERATOR, OPERATIONS_MANAGER, FIELD_ENGINEER, or ADMIN; actor access is checked", true);
        add(docs, "PATCH", "/api/work-orders/{id}/assign", "Work Orders", "Assign a work order", "Assigns a work order to the supplied field engineer ID.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "PATCH", "/api/work-orders/{id}/status", "Work Orders", "Transition work-order status", "Requests a supported work-order transition. The backend validates the state transition and actor.", "OPERATOR, OPERATIONS_MANAGER, FIELD_ENGINEER, or ADMIN; transition rules still apply", true);
        add(docs, "POST", "/api/work-orders/{id}/accept", "Work Orders", "Accept assigned work", "Accepts a work order for the authenticated field engineer when assignment permits.", "FIELD_ENGINEER with matching assignment", true);
        add(docs, "POST", "/api/work-orders/{id}/start", "Work Orders", "Start work", "Starts an assigned work order subject to the backend transition and ownership checks.", "FIELD_ENGINEER with matching assignment", true);
        add(docs, "POST", "/api/work-orders/{id}/complete", "Work Orders", "Submit repair findings", "Submits inspection and repair findings to complete the work-order step.", "FIELD_ENGINEER with matching assignment", true);
        add(docs, "POST", "/api/work-orders/{id}/verify", "Operations", "Verify a completed repair", "Records an authority approval or rejection for completed repair work.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/work-orders/{id}/evidence", "Work Orders", "Upload work-order evidence", "Uploads one evidence file with its evidence type using multipart/form-data.", "FIELD_ENGINEER; work-order access is also checked", true);
        add(docs, "GET", "/api/work-orders/{id}/evidence", "Work Orders", "List work-order evidence", "Lists evidence metadata for an accessible work order.", "OPERATOR, OPERATIONS_MANAGER, FIELD_ENGINEER, or ADMIN; access is checked", true);
        add(docs, "GET", "/api/evidence/{id}", "Work Orders", "Download evidence", "Downloads evidence content after actor authorization.", "OPERATOR, OPERATIONS_MANAGER, FIELD_ENGINEER, or ADMIN; access is checked", true);
        add(docs, "GET", "/api/analytics/overview", "Analytics", "Get operational analytics", "Returns database-derived incident, work-order, resolution, workload, and trend aggregates.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "GET", "/api/manager/dashboard", "Operations", "Get the manager dashboard", "Returns the existing operations-manager dashboard data.", "OPERATIONS_MANAGER or ADMIN", true);
        add(docs, "GET", "/api/admin/users", "Admin", "List users", "Returns administrative user summaries and assigned role names.", "ADMIN", true);
        add(docs, "GET", "/api/admin/roles", "Admin", "List roles", "Returns the roles configured in the system.", "ADMIN", true);
        add(docs, "GET", "/api/admin/zones", "Admin", "List water zones", "Returns administrative water-zone records.", "ADMIN", true);
        add(docs, "GET", "/api/admin/infrastructure/assets", "Admin", "List infrastructure assets", "Returns administrative infrastructure-asset records.", "ADMIN", true);
        add(docs, "POST", "/api/admin/users/{userId}/roles", "Admin", "Assign a privileged role", "Assigns an allowed non-CITIZEN role to the target account. Public registration cannot assign privileged roles.", "ADMIN", true);
        add(docs, "GET", "/api/infrastructure/assets", "Operations", "List active infrastructure assets", "Returns active infrastructure assets for operational views; data is the configured/synthetic model, not live SCADA telemetry.", "OPERATOR, OPERATIONS_MANAGER, FIELD_ENGINEER, or ADMIN", true);
        add(docs, "GET", "/api/infrastructure/zones", "Operations", "List water zones", "Returns configured water zones for authorized operational views.", "OPERATOR, OPERATIONS_MANAGER, FIELD_ENGINEER, or ADMIN", true);
        add(docs, "GET", "/api/notifications", "Notifications", "List current-user notifications", "Returns notifications belonging to the authenticated account.", "Authenticated user; results are account-scoped", true);
        add(docs, "GET", "/api/notifications/unread", "Notifications", "List unread notifications", "Returns unread notifications belonging to the authenticated account.", "Authenticated user; results are account-scoped", true);
        add(docs, "POST", "/api/notifications/{id}/read", "Notifications", "Mark a notification as read", "Marks an owned notification as read.", "Authenticated user; notification ownership checked", true);
        add(docs, "GET", "/api/health", "System", "Check application health", "Returns the basic application health response.", "Public", false);
        add(docs, "POST", "/api/ivr/sessions", "IVR", "Start an authenticated IVR session", "Starts a provider-neutral IVR session for the development/operator workflow.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/sessions/{id}/language", "IVR", "Select IVR language", "Selects a supported language for the session.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/sessions/{id}/menu", "IVR", "Submit a menu choice", "Advances the IVR session using a supported menu choice.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/sessions/{id}/details", "IVR", "Submit incident details", "Adds the selected incident category and caller-provided description.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/sessions/{id}/location", "IVR", "Submit location metadata", "Stores supplied coordinates and their location source/accuracy; ordinary phone calls do not automatically provide GPS.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/sessions/{id}/confirmation", "IVR", "Confirm or cancel incident creation", "Confirms creation through the shared incident service or cancels the session.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "GET", "/api/ivr/sessions/{id}", "IVR", "Get an IVR session", "Returns a session in the authenticated IVR workflow.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "GET", "/api/ivr/sessions/{id}/complaints", "IVR", "List complaints for an IVR session", "Returns IVR-linked complaint records associated with the session caller.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/sessions/{sessionId}/escalate", "IVR Escalation", "Create an operator escalation", "Queues an escalation for an active owned session. The reason must be nonblank and at most 1,000 characters.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "GET", "/api/ivr/escalations/pending", "IVR Escalation", "List pending escalations", "Returns queued operator escalations.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/escalations/{id}/accept", "IVR Escalation", "Accept an escalation", "Accepts a queued escalation. The assigned operator is derived from the authenticated JWT principal, not a caller-supplied ID.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/escalations/{id}/complete", "IVR Escalation", "Complete an escalation", "Completes an escalation in a valid in-progress state.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/escalations/{id}/cancel", "IVR Escalation", "Cancel an escalation", "Cancels an escalation that has not already been completed or cancelled.", "OPERATOR, OPERATIONS_MANAGER, or ADMIN", true);
        add(docs, "POST", "/api/ivr/gateway/sessions", "IVR Gateway", "Start a trusted-provider IVR session", "Creates a session after validating provider trust. Caller identity is taken from X-IVR-Caller-Id; an optional matching body field is accepted, but cannot override the header.", "Public route protected by X-IVR-Trust and X-IVR-Caller-Id; no bearer JWT", false);
        add(docs, "GET", "/api/ivr/gateway/sessions/{id}", "IVR Gateway", "Get an owned gateway session", "Returns a session only when the trusted caller header matches its bound caller identity.", "X-IVR-Trust and X-IVR-Caller-Id required; no bearer JWT", false);
        add(docs, "GET", "/api/ivr/gateway/sessions/{id}/complaints", "IVR Gateway", "Get caller-owned complaint status", "Returns caller-safe status information only for the session owner.", "X-IVR-Trust and X-IVR-Caller-Id required; no bearer JWT", false);
        add(docs, "POST", "/api/ivr/gateway/sessions/{id}/escalate", "IVR Gateway", "Request a trusted-provider escalation", "Queues an escalation for the active session bound to the trusted caller identity.", "X-IVR-Trust and X-IVR-Caller-Id required; no bearer JWT", false);
        return docs;
    }

    private void add(Map<String, EndpointDoc> docs, String method, String path, String tag,
            String summary, String description, String access, boolean jwtRequired) {
        docs.put(method + " " + path, new EndpointDoc(tag, summary, description, access, jwtRequired));
    }

    private record EndpointDoc(String tag, String summary, String description, String access, boolean jwtRequired) {
    }
}