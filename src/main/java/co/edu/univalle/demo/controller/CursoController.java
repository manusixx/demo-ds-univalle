package co.edu.univalle.demo.controller;

import co.edu.univalle.demo.model.CursoModel;
import co.edu.univalle.demo.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de cursos y matrículas académicas.
 * Este controller es el más completo del sistema — expone tanto el CRUD
 * básico del curso como la lógica de matrícula y retiro de estudiantes.
 */
@RestController
@RequestMapping("/api/v1/cursos")
@Tag(name = "Cursos", description = "Gestión de cursos y matrículas académicas")
public class CursoController {

    /** Servicio de lógica de negocio para cursos. */
    private final CursoService cursoService;

    /**
     * Constructor con inyección de dependencias.
     * @param cursoService servicio de cursos
     */
    public CursoController(final CursoService cursoService) {
        this.cursoService = cursoService;
    }

    /**
     * Crea un nuevo curso asignado a un profesor.
     * El profesorId va como query parameter porque es una referencia
     * a una entidad existente — no es un campo del curso en sí.
     * @param curso datos del curso a crear
     * @param profesorId id del profesor que oferta el curso
     * @return el curso creado con HTTP 201
     */
    @PostMapping
    @Operation(summary = "Crear curso",
            description = "Registra un nuevo curso asignado a un profesor existente")
    public ResponseEntity<CursoModel> crear(
            @RequestBody final CursoModel curso,
            @RequestParam final Long profesorId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cursoService.crear(curso, profesorId));
    }

    /**
     * Actualiza los datos básicos de un curso.
     * No permite cambiar el profesor ni los estudiantes —
     * esas operaciones tienen endpoints dedicados.
     * @param curso datos actualizados del curso con id válido
     * @return el curso actualizado con HTTP 200
     */
    @PutMapping
    @Operation(summary = "Actualizar curso",
            description = "Actualiza nombre, créditos y semestre de un curso existente")
    public ResponseEntity<CursoModel> actualizar(
            @RequestBody final CursoModel curso) {
        return ResponseEntity.ok(cursoService.actualizar(curso));
    }

    /**
     * Elimina un curso por su id.
     * Las matrículas asociadas se eliminan automáticamente
     * por el ON DELETE CASCADE en la tabla curso_estudiante.
     * @param id identificador del curso a eliminar
     * @return HTTP 204 sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar curso",
            description = "Elimina un curso y todas sus matrículas asociadas")
    public ResponseEntity<Void> eliminar(@PathVariable final Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna todos los cursos registrados.
     * @return lista de cursos con HTTP 200
     */
    @GetMapping
    @Operation(summary = "Listar cursos",
            description = "Retorna todos los cursos registrados en el sistema")
    public ResponseEntity<List<CursoModel>> obtenerTodos() {
        return ResponseEntity.ok(cursoService.obtenerTodos());
    }

    /**
     * Busca un curso por su id.
     * @param id identificador del curso
     * @return el curso encontrado con HTTP 200
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar por id",
            description = "Retorna un curso específico por su id")
    public ResponseEntity<CursoModel> obtenerPorId(
            @PathVariable final Long id) {
        return ResponseEntity.ok(cursoService.obtenerPorId(id));
    }

    /**
     * Matricula un estudiante en un curso.
     * Usa POST porque es una operación que crea una relación
     * entre dos entidades existentes — un registro en curso_estudiante.
     * @param cursoId id del curso
     * @param estudianteId id del estudiante a matricular
     * @return el curso actualizado con el estudiante matriculado — HTTP 200
     */
    @PostMapping("/{cursoId}/matricular/{estudianteId}")
    @Operation(summary = "Matricular estudiante",
            description = "Matricula un estudiante en un curso específico")
    public ResponseEntity<CursoModel> matricularEstudiante(
            @PathVariable final Long cursoId,
            @PathVariable final Long estudianteId) {
        return ResponseEntity.ok(
                cursoService.matricularEstudiante(cursoId, estudianteId));
    }

    /**
     * Retira un estudiante de un curso.
     * Usa DELETE porque elimina la relación entre el curso y el estudiante
     * — borra el registro correspondiente en curso_estudiante.
     * @param cursoId id del curso
     * @param estudianteId id del estudiante a retirar
     * @return el curso actualizado sin el estudiante — HTTP 200
     */
    @DeleteMapping("/{cursoId}/retirar/{estudianteId}")
    @Operation(summary = "Retirar estudiante",
            description = "Retira un estudiante matriculado de un curso")
    public ResponseEntity<CursoModel> retirarEstudiante(
            @PathVariable final Long cursoId,
            @PathVariable final Long estudianteId) {
        return ResponseEntity.ok(
                cursoService.retirarEstudiante(cursoId, estudianteId));
    }

    /**
     * Busca cursos por periodo académico.
     * @param semestre periodo académico, ej: 2026-1
     * @return lista de cursos en ese semestre con HTTP 200
     */
    @GetMapping("/semestre")
    @Operation(summary = "Cursos por semestre",
            description = "Retorna todos los cursos de un periodo académico específico")
    public ResponseEntity<List<CursoModel>> buscarPorSemestre(
            @RequestParam final String semestre) {
        return ResponseEntity.ok(cursoService.buscarPorSemestre(semestre));
    }

    /**
     * Busca cursos asignados a un profesor.
     * @param profesorId id del profesor
     * @return lista de cursos del profesor con HTTP 200
     */
    @GetMapping("/profesor/{profesorId}")
    @Operation(summary = "Cursos por profesor",
            description = "Retorna todos los cursos asignados a un profesor específico")
    public ResponseEntity<List<CursoModel>> buscarPorProfesor(
            @PathVariable final Long profesorId) {
        return ResponseEntity.ok(cursoService.buscarPorProfesor(profesorId));
    }

    /**
     * Busca cursos en los que está matriculado un estudiante.
     * @param estudianteId id del estudiante
     * @return lista de cursos del estudiante con HTTP 200
     */
    @GetMapping("/estudiante/{estudianteId}")
    @Operation(summary = "Cursos por estudiante",
            description = "Retorna todos los cursos en los que está matriculado un estudiante")
    public ResponseEntity<List<CursoModel>> buscarPorEstudiante(
            @PathVariable final Long estudianteId) {
        return ResponseEntity.ok(
                cursoService.buscarPorEstudiante(estudianteId));
    }
}