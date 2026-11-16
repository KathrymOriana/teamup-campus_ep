package com.campus.teamup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/** Prueba de extremo a extremo con un servidor real: sin sesión 401, con sesión + CSRF 200, y avance calculado. */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:teamup-test",
                "spring.datasource.username=sa",
                "spring.datasource.password=" })
class ApiFlowTest {

    @Value("${local.server.port}")
    int port;

    private final CookieManager cookies = new CookieManager();
    private final HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).build();

    private HttpResponse<String> send(String method, String path, String json, String csrf) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (csrf != null) {
            builder.header("X-XSRF-TOKEN", csrf);
        }
        builder.method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        return client.send(builder.build(), BodyHandlers.ofString());
    }

    private String csrfToken() {
        return cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals("XSRF-TOKEN")).findFirst().orElseThrow().getValue();
    }

    @Test
    void apiRequiereSesionYDevuelveDatosCalculadosPorElServidor() throws Exception {
        assertEquals(401, send("GET", "/api/projects", null, null).statusCode());

        send("GET", "/login.html", null, null); // el servidor entrega la cookie XSRF-TOKEN
        HttpResponse<String> login = send("POST", "/api/auth/login",
                "{\"email\":\"ana.lopez@campus.edu\",\"password\":\"teamup123\"}", csrfToken());
        assertEquals(200, login.statusCode());

        HttpResponse<String> projects = send("GET", "/api/projects", null, null);
        assertEquals(200, projects.statusCode());
        assertTrue(projects.body().contains("Sistema de gestión de biblioteca"));
        assertTrue(projects.body().contains("\"avance\""));

        assertEquals(401, send("POST", "/api/auth/login",
                "{\"email\":\"ana.lopez@campus.edu\",\"password\":\"mala\"}", csrfToken()).statusCode());
    }
}

