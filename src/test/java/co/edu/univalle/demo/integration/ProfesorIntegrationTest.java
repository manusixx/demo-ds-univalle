package co.edu.univalle.demo.integration;

import co.edu.univalle.demo.model.ProfesorModel;
import co.edu.univalle.demo.repository.ProfesorRepository;

// @SpringBootTest levanta el servidor HTTP completo en un puerto aleatorio
// TestRestTemplate hace peticiones HTTP reales a ese servidor
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Hereda de BaseIntegrationTest que ya configura Testcontainers y PostgreSQL real
// Esta clase NO necesita repetir @SpringBootTest ni @Testcontainers
@DisplayName("Tests de integración - Profesor")
class ProfesorIntegrationTest extends BaseIntegrationTest {

    // TestRestTemplate hace peticiones HTTP reales al servidor levantado por @SpringBootTest
    // A diferencia de MockMvc que simula el servlet, TestRestTemplate prueba
    // el stack completo incluyendo serialización JSON y códigos HTTP reales
    @Autowired
    private TestRestTemplate restTemplate;

    // Inyectamos el repositorio directamente para preparar datos de prueba
    // y limpiar la BD entre tests — sin pasar por el controller ni el service
    @Autowired
    private ProfesorRepository profesorRepository;

    // @Value inyecta el puerto aleatorio que @SpringBootTest asignó al servidor
    // No podemos hardcode 8085 porque en tests el puerto es aleatorio
    @Value("${local.server.port}")
    private int port;

    // Método helper que construye la URL base del endpoint de profesores
    // incluye el context-path /demo-ds-univalle y la versión /api/v1
    private String baseUrl() {
        return "http://localhost:" + port + "/demo-ds-univalle/api/v1/profesores";
    }

    // Paso 1: @BeforeEach — limpiar la BD
    // Se ejecuta ANTES de cada test individual
    // Elimina todos los registros de la tabla profesor
    // garantiza que cada test parte de una BD vacía y limpia
    // sin esto, datos creados en un test podrían afectar al siguiente
    @BeforeEach
    void limpiarBaseDeDatos() {
        profesorRepository.deleteAll();
    }

    // Método helper que construye un ProfesorModel de prueba sin id
    // el id va vacío porque la BD lo asigna automáticamente al guardar
    private ProfesorModel buildProfesor() {
        return ProfesorModel.builder()
                .nombre("Carlos")
                .apellido("Ramírez")
                .email("carlos@univalle.edu.co")
                .identificacion("12345678")
                .build();
    }

    // ── Crear ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/profesores - crea profesor y retorna HTTP 201")
    void whenPostProfesor_thenReturnCreated() {
        // Paso 2: preparamos el dato a enviar — sin ID, la BD lo asigna
        var profesor = buildProfesor();

        // Paso 3: ejecutamos la petición HTTP POST real contra el servidor
        // postForEntity envía el objeto como JSON y recibe la respuesta
        ResponseEntity<ProfesorModel> response = restTemplate.postForEntity(
                baseUrl(), profesor, ProfesorModel.class);

        // Paso 4: verificamos el código HTTP y el cuerpo de la respuesta
        // debe retornar 201 Created con el profesor guardado y su ID asignado
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Carlos", response.getBody().getNombre());
    }

    @Test
    @DisplayName("POST /api/v1/profesores - retorna HTTP 409 con email duplicado")
    void whenPostProfesor_withDuplicateEmail_thenReturnConflict() {
        // Paso 2: guardamos un profesor directamente en BD via repositorio
        // esto simula que ya existe ese email antes de intentar crearlo de nuevo
        profesorRepository.save(buildProfesor());

        // Paso 3: intentamos crear otro profesor con el mismo email
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl(), buildProfesor(), String.class);

        // Paso 4: debe retornar 409 Conflict — email duplicado
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // ── Obtener todos ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/profesores - retorna lista de profesores con HTTP 200")
    void whenGetAllProfesores_thenReturnList() {
        // Paso 2: guardamos un profesor directamente en BD
        profesorRepository.save(buildProfesor());

        // Paso 3: ejecutamos GET para obtener todos los profesores
        // usamos String.class para evitar problemas con tipos genéricos en listas
        // el contenido exacto de la lista lo verifican los tests unitarios
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl(), String.class);

        // Paso 4: debe retornar 200 OK con contenido en el cuerpo
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ── Obtener por id ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/profesores/{id} - retorna profesor con HTTP 200")
    void whenGetProfesorById_withExistingId_thenReturnProfesor() {
        // Paso 2: guardamos y capturamos el profesor con su ID asignado por la BD
        ProfesorModel saved = profesorRepository.save(buildProfesor());

        // Paso 3: ejecutamos GET con el id real asignado por PostgreSQL
        ResponseEntity<ProfesorModel> response = restTemplate.getForEntity(
                baseUrl() + "/" + saved.getId(), ProfesorModel.class);

        // Paso 4: debe retornar 200 OK con el profesor correcto
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(saved.getId(), response.getBody().getId());
    }

    @Test
    @DisplayName("GET /api/v1/profesores/{id} - retorna HTTP 404 con id inexistente")
    void whenGetProfesorById_withNonExistingId_thenReturnNotFound() {
        // Paso 2: la BD está vacía — no preparamos ningún dato
        // Paso 3: buscamos un id que no existe
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/999", String.class);

        // Paso 4: debe retornar 404 Not Found
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/v1/profesores - actualiza profesor y retorna HTTP 200")
    void whenPutProfesor_withExistingId_thenReturnUpdated() {
        // Paso 2: guardamos el profesor y modificamos su nombre
        ProfesorModel saved = profesorRepository.save(buildProfesor());
        saved.setNombre("Carlos Actualizado");

        // Paso 3: ejecutamos PUT enviando el profesor modificado
        // HttpEntity envuelve el objeto para enviarlo como cuerpo de la petición
        ResponseEntity<ProfesorModel> response = restTemplate.exchange(
                baseUrl(), HttpMethod.PUT,
                new HttpEntity<>(saved), ProfesorModel.class);

        // Paso 4: debe retornar 200 OK con el nombre actualizado
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Carlos Actualizado", response.getBody().getNombre());
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/v1/profesores/{id} - elimina profesor y retorna HTTP 204")
    void whenDeleteProfesor_withExistingId_thenReturnNoContent() {
        // Paso 2: guardamos el profesor para tener un ID válido que eliminar
        ProfesorModel saved = profesorRepository.save(buildProfesor());

        // Paso 3: ejecutamos DELETE con el id real
        // exchange permite especificar el método HTTP y el tipo de respuesta
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + saved.getId(),
                HttpMethod.DELETE, null, Void.class);

        // Paso 4: debe retornar 204 No Content — eliminado exitosamente
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /api/v1/profesores/{id} - retorna HTTP 404 con id inexistente")
    void whenDeleteProfesorById_withNonExistingId_thenReturnNotFound() {
        // Paso 2: la BD está vacía — no preparamos ningún dato
        // Paso 3: intentamos eliminar un id que no existe
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/999",
                HttpMethod.DELETE, null, String.class);

        // Paso 4: debe retornar 404 Not Found
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Buscar por nombre

    @Test
    @DisplayName("GET /api/v1/profesores/buscar - retorna profesores que coinciden")
    void whenSearchByNombre_thenReturnMatchingProfesores() {
        // Paso 2: guardamos un profesor con nombre "Carlos"
        profesorRepository.save(buildProfesor());

        // Paso 3: buscamos por el fragmento "Carlos"
        // usamos String.class para evitar problemas con tipos genéricos en listas
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/buscar?nombre=Carlos", String.class);

        // Paso 4: debe retornar 200 OK con contenido en el cuerpo
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}