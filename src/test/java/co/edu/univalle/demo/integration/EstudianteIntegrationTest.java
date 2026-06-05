package co.edu.univalle.demo.integration;

import co.edu.univalle.demo.model.EstudianteModel;
import co.edu.univalle.demo.repository.EstudianteRepository;
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
@DisplayName("Tests de integración - Estudiante")
class EstudianteIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Value("${local.server.port}")
    private int port;

    private String baseUrl() {
        return "http://localhost:" + port + "/demo-ds-univalle/api/v1/estudiantes";
    }

    // Paso 1: limpiar la BD antes de cada test
    // garantiza que cada test parte de un estado conocido y limpio
    @BeforeEach
    void limpiarBaseDeDatos() {
        estudianteRepository.deleteAll();
    }

    // Helper que construye un EstudianteModel válido sin id
    // el id lo asigna PostgreSQL automáticamente al guardar
    private EstudianteModel buildEstudiante() {
        return EstudianteModel.builder()
                .nombre("Ana")
                .apellido("García")
                .email("ana@univalle.edu.co")
                .identificacion("87654321")
                .semestre(4)
                .build();
    }

    // Crear

    @Test
    @DisplayName("POST /api/v1/estudiantes - crea estudiante y retorna HTTP 201")
    void whenPostEstudiante_thenReturnCreated() {
        // Paso 2: preparamos el estudiante sin id
        var estudiante = buildEstudiante();

        // Paso 3: POST real contra el servidor
        ResponseEntity<EstudianteModel> response = restTemplate.postForEntity(
                baseUrl(), estudiante, EstudianteModel.class);

        // Paso 4: debe retornar 201 Created con el estudiante guardado
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Ana", response.getBody().getNombre());
        assertEquals(4, response.getBody().getSemestre());
    }

    @Test
    @DisplayName("POST /api/v1/estudiantes - retorna HTTP 409 con email duplicado")
    void whenPostEstudiante_withDuplicateEmail_thenReturnConflict() {
        // Paso 2: guardamos un estudiante con ese email
        estudianteRepository.save(buildEstudiante());

        // Paso 3: intentamos crear otro con el mismo email
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl(), buildEstudiante(), String.class);

        // Paso 4: debe retornar 409 Conflict — email duplicado
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // Caso de prueba específico del Paso 8 — semestre inválido en integración
    // verifica que la validación del service llega correctamente al cliente
    @Test
    @DisplayName("POST /api/v1/estudiantes - retorna HTTP 409 con semestre inválido")
    void whenPostEstudiante_withInvalidSemestre_thenReturnConflict() {
        // Paso 2: construimos un estudiante con semestre fuera del rango
        var estudiante = EstudianteModel.builder()
                .nombre("Luis")
                .apellido("Torres")
                .email("luis@univalle.edu.co")
                .identificacion("11111111")
                .semestre(11)   // ← fuera del rango permitido (1-10)
                .build();

        // Paso 3: POST con semestre inválido
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl(), estudiante, String.class);

        // Paso 4: debe retornar 409 Conflict — semestre inválido
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // Obtener todos

    @Test
    @DisplayName("GET /api/v1/estudiantes - retorna lista con HTTP 200")
    void whenGetAllEstudiantes_thenReturnList() {
        // Paso 2: guardamos un estudiante directamente en BD
        estudianteRepository.save(buildEstudiante());

        // Paso 3: GET para obtener todos los estudiantes
        // usamos String.class para evitar problemas con tipos genéricos
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl(), String.class);

        // Paso 4: debe retornar 200 OK con contenido
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // Obtener por id

    @Test
    @DisplayName("GET /api/v1/estudiantes/{id} - retorna estudiante con HTTP 200")
    void whenGetEstudianteById_withExistingId_thenReturnEstudiante() {
        // Paso 2: guardamos y capturamos el estudiante con su id asignado
        EstudianteModel saved = estudianteRepository.save(buildEstudiante());

        // Paso 3: GET con el id real asignado por PostgreSQL
        ResponseEntity<EstudianteModel> response = restTemplate.getForEntity(
                baseUrl() + "/" + saved.getId(), EstudianteModel.class);

        // Paso 4: debe retornar 200 OK con el estudiante correcto
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(saved.getId(), response.getBody().getId());
        assertEquals(4, response.getBody().getSemestre());
    }

    @Test
    @DisplayName("GET /api/v1/estudiantes/{id} - retorna HTTP 404 con id inexistente")
    void whenGetEstudianteById_withNonExistingId_thenReturnNotFound() {
        // Paso 2: BD vacía — no preparamos ningún dato
        // Paso 3: buscamos un id que no existe
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/999", String.class);

        // Paso 4: debe retornar 404 Not Found
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Actualizar

    @Test
    @DisplayName("PUT /api/v1/estudiantes - actualiza estudiante y retorna HTTP 200")
    void whenPutEstudiante_withExistingId_thenReturnUpdated() {
        // Paso 2: guardamos el estudiante y modificamos su nombre
        EstudianteModel saved = estudianteRepository.save(buildEstudiante());
        saved.setNombre("Ana Actualizada");

        // Paso 3: PUT enviando el estudiante modificado
        ResponseEntity<EstudianteModel> response = restTemplate.exchange(
                baseUrl(), HttpMethod.PUT,
                new HttpEntity<>(saved), EstudianteModel.class);

        // Paso 4: debe retornar 200 OK con el nombre actualizado
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ana Actualizada", response.getBody().getNombre());
    }

    @Test
    @DisplayName("PUT /api/v1/estudiantes - retorna HTTP 409 con semestre inválido al actualizar")
    void whenPutEstudiante_withInvalidSemestre_thenReturnConflict() {
        // Paso 2: guardamos el estudiante y modificamos el semestre a inválido
        EstudianteModel saved = estudianteRepository.save(buildEstudiante());
        saved.setSemestre(11);  // ← inválido

        // Paso 3: PUT con semestre inválido
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl(), HttpMethod.PUT,
                new HttpEntity<>(saved), String.class);

        // Paso 4: debe retornar 409 Conflict — semestre inválido
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // Eliminar

    @Test
    @DisplayName("DELETE /api/v1/estudiantes/{id} - elimina y retorna HTTP 204")
    void whenDeleteEstudiante_withExistingId_thenReturnNoContent() {
        // Paso 2: guardamos el estudiante para tener un id válido
        EstudianteModel saved = estudianteRepository.save(buildEstudiante());

        // Paso 3: DELETE con el id real
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + saved.getId(),
                HttpMethod.DELETE, null, Void.class);

        // Paso 4: debe retornar 204 No Content — eliminado exitosamente
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /api/v1/estudiantes/{id} - retorna HTTP 404 con id inexistente")
    void whenDeleteEstudiante_withNonExistingId_thenReturnNotFound() {
        // Paso 2: BD vacía — no preparamos ningún dato
        // Paso 3: intentamos eliminar un id que no existe
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/999",
                HttpMethod.DELETE, null, String.class);

        // Paso 4: debe retornar 404 Not Found
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Buscar por nombre

    @Test
    @DisplayName("GET /api/v1/estudiantes/buscar - retorna estudiantes que coinciden")
    void whenSearchByNombre_thenReturnMatchingEstudiantes() {
        // Paso 2: guardamos un estudiante con nombre "Ana"
        estudianteRepository.save(buildEstudiante());

        // Paso 3: buscamos por el fragmento "Ana"
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/buscar?nombre=Ana", String.class);

        // Paso 4: debe retornar 200 OK con contenido
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // Buscar por semestre

    // Caso de prueba específico del Paso 8 en integración
    // verifica que la búsqueda por semestre funciona contra PostgreSQL real
    @Test
    @DisplayName("GET /api/v1/estudiantes/buscar/semestre - retorna estudiantes del semestre")
    void whenSearchBySemestre_thenReturnMatchingEstudiantes() {
        // Paso 2: guardamos un estudiante de semestre 4
        estudianteRepository.save(buildEstudiante());

        // Paso 3: buscamos por semestre 4
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/buscar/semestre?semestre=4", String.class);

        // Paso 4: debe retornar 200 OK con contenido
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}