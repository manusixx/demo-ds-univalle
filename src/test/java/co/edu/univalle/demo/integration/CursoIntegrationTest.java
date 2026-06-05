package co.edu.univalle.demo.integration;

import co.edu.univalle.demo.model.CursoModel;
import co.edu.univalle.demo.model.EstudianteModel;
import co.edu.univalle.demo.model.ProfesorModel;
import co.edu.univalle.demo.repository.CursoRepository;
import co.edu.univalle.demo.repository.EstudianteRepository;
import co.edu.univalle.demo.repository.ProfesorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Hereda de BaseIntegrationTest que ya configura Testcontainers y PostgreSQL real
@DisplayName("Tests de integración - Curso")
class CursoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Value("${local.server.port}")
    private int port;

    private String baseUrl() {
        return "http://localhost:" + port + "/demo-ds-univalle/api/v1/cursos";
    }

    // Objetos compartidos entre tests — se crean en @BeforeEach
    private ProfesorModel profesorGuardado;
    private EstudianteModel estudianteGuardado;

    // Paso 1: limpiar la BD y preparar datos base antes de cada test
    // El orden importa: primero cursos (tiene FK hacia profesor y estudiante),
    // luego estudiantes, luego profesores
    @BeforeEach
    void prepararDatos() {
        cursoRepository.deleteAll();
        estudianteRepository.deleteAll();
        profesorRepository.deleteAll();

        // Guardamos un profesor y un estudiante base para usar en los tests
        profesorGuardado = profesorRepository.save(
                ProfesorModel.builder()
                        .nombre("Carlos")
                        .apellido("Ramírez")
                        .email("carlos@univalle.edu.co")
                        .identificacion("12345678")
                        .build());

        estudianteGuardado = estudianteRepository.save(
                EstudianteModel.builder()
                        .nombre("Ana")
                        .apellido("García")
                        .email("ana@univalle.edu.co")
                        .identificacion("87654321")
                        .semestre(4)
                        .build());
    }

    // Helper para construir un CursoModel de prueba sin id ni profesor
    // el profesor se pasa como query param en el POST
    private CursoModel buildCurso() {
        return CursoModel.builder()
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .build();
    }

    // Crear

    @Test
    @DisplayName("POST /api/v1/cursos - crea curso y retorna HTTP 201")
    void whenPostCurso_thenReturnCreated() {
        // Paso 2: preparamos el curso sin id — el profesorId va como query param
        var curso = buildCurso();

        // Paso 3: POST con profesorId como query parameter
        ResponseEntity<CursoModel> response = restTemplate.postForEntity(
                baseUrl() + "?profesorId=" + profesorGuardado.getId(),
                curso, CursoModel.class);

        // Paso 4: debe retornar 201 Created con el curso guardado
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("DS-101", response.getBody().getCodigo());
    }

    @Test
    @DisplayName("POST /api/v1/cursos - retorna HTTP 404 con profesor inexistente")
    void whenPostCurso_withNonExistingProfesor_thenReturnNotFound() {
        // Paso 2: usamos un profesorId que no existe
        var curso = buildCurso();

        // Paso 3: POST con id de profesor inexistente
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "?profesorId=999",
                curso, String.class);

        // Paso 4: debe retornar 404 Not Found
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/v1/cursos - retorna HTTP 409 con código duplicado")
    void whenPostCurso_withDuplicateCodigo_thenReturnConflict() {
        // Paso 2: guardamos un curso con el código DS-101
        cursoRepository.save(CursoModel.builder()
                .nombre("Curso existente")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(profesorGuardado)
                .build());

        // Paso 3: intentamos crear otro curso con el mismo código
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "?profesorId=" + profesorGuardado.getId(),
                buildCurso(), String.class);

        // Paso 4: debe retornar 409 Conflict — código duplicado
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // Obtener todos

    @Test
    @DisplayName("GET /api/v1/cursos - retorna lista con HTTP 200")
    void whenGetAllCursos_thenReturnList() {
        // Paso 2: guardamos un curso directamente en BD
        cursoRepository.save(CursoModel.builder()
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(profesorGuardado)
                .build());

        // Paso 3: GET para obtener todos los cursos
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl(), String.class);

        // Paso 4: debe retornar 200 OK con contenido
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // Obtener por id

    @Test
    @DisplayName("GET /api/v1/cursos/{id} - retorna curso con HTTP 200")
    void whenGetCursoById_withExistingId_thenReturnCurso() {
        // Paso 2: guardamos el curso y capturamos su id
        CursoModel saved = cursoRepository.save(CursoModel.builder()
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(profesorGuardado)
                .build());

        // Paso 3: GET con el id real asignado por PostgreSQL
        ResponseEntity<CursoModel> response = restTemplate.getForEntity(
                baseUrl() + "/" + saved.getId(), CursoModel.class);

        // Paso 4: debe retornar 200 OK con el curso correcto
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(saved.getId(), response.getBody().getId());
    }

    @Test
    @DisplayName("GET /api/v1/cursos/{id} - retorna HTTP 404 con id inexistente")
    void whenGetCursoById_withNonExistingId_thenReturnNotFound() {
        // Paso 2: BD vacía de cursos
        // Paso 3: buscamos un id que no existe
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/999", String.class);

        // Paso 4: debe retornar 404 Not Found
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // Matricular

    // Este test verifica el flujo completo de matrícula contra PostgreSQL real
    // La tabla intermedia curso_estudiante debe recibir el registro correctamente
    @Test
    @DisplayName("POST /{cursoId}/matricular/{estudianteId} - matricula y retorna HTTP 200")
    void whenMatricularEstudiante_thenReturnOk() {
        // Paso 2: guardamos el curso
        CursoModel saved = cursoRepository.save(CursoModel.builder()
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(profesorGuardado)
                .build());

        // Paso 3: POST para matricular al estudiante en el curso
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/" + saved.getId()
                        + "/matricular/" + estudianteGuardado.getId(),
                null, String.class);

        // Paso 4: debe retornar 200 OK
        // no verificamos el Set directamente por FetchType.LAZY —
        // verificamos el código HTTP que confirma que la operación fue exitosa
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("POST /{cursoId}/matricular/{estudianteId} - retorna HTTP 409 si ya matriculado")
    void whenMatricularEstudiante_alreadyEnrolled_thenReturnConflict() {
        // Paso 2: guardamos el curso y matriculamos al estudiante
        CursoModel saved = cursoRepository.save(CursoModel.builder()
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(profesorGuardado)
                .build());

        // Primera matrícula — debe ser exitosa
        restTemplate.postForEntity(
                baseUrl() + "/" + saved.getId()
                        + "/matricular/" + estudianteGuardado.getId(),
                null, String.class);

        // Paso 3: intentamos matricular de nuevo al mismo estudiante
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/" + saved.getId()
                        + "/matricular/" + estudianteGuardado.getId(),
                null, String.class);

        // Paso 4: debe retornar 409 Conflict — ya matriculado
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // Retirar

    @Test
    @DisplayName("DELETE /{cursoId}/retirar/{estudianteId} - retira y retorna HTTP 200")
    void whenRetirarEstudiante_thenReturnOk() {
        // Paso 2: guardamos el curso y matriculamos al estudiante
        CursoModel saved = cursoRepository.save(CursoModel.builder()
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(profesorGuardado)
                .build());

        // Primero matriculamos
        restTemplate.postForEntity(
                baseUrl() + "/" + saved.getId()
                        + "/matricular/" + estudianteGuardado.getId(),
                null, String.class);

        // Paso 3: DELETE para retirar al estudiante
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/" + saved.getId()
                        + "/retirar/" + estudianteGuardado.getId(),
                HttpMethod.DELETE, null, String.class);

        // Paso 4: debe retornar 200 OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /{cursoId}/retirar/{estudianteId} - retorna HTTP 409 si no matriculado")
    void whenRetirarEstudiante_notEnrolled_thenReturnConflict() {
        // Paso 2: guardamos el curso pero NO matriculamos al estudiante
        CursoModel saved = cursoRepository.save(CursoModel.builder()
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(profesorGuardado)
                .build());

        // Paso 3: intentamos retirar a un estudiante que no está matriculado
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/" + saved.getId()
                        + "/retirar/" + estudianteGuardado.getId(),
                HttpMethod.DELETE, null, String.class);

        // Paso 4: debe retornar 409 Conflict — no está matriculado
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // Cursos por profesor

    @Test
    @DisplayName("GET /profesor/{profesorId} - retorna cursos del profesor")
    void whenGetCursosByProfesor_thenReturnList() {
        // Paso 2: guardamos un curso asignado al profesor guardado
        cursoRepository.save(CursoModel.builder()
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(profesorGuardado)
                .build());

        // Paso 3: GET para obtener cursos del profesor
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/profesor/" + profesorGuardado.getId(),
                String.class);

        // Paso 4: debe retornar 200 OK con contenido
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}