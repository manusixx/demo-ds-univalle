package co.edu.univalle.demo.controller;

import co.edu.univalle.demo.model.EstudianteModel;
import co.edu.univalle.demo.service.EstudianteService;
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
 * Controlador REST para la gestión de estudiantes.
 */
@RestController
@RequestMapping("/api/v1/estudiantes")
@Tag(name = "Estudiantes", description = "Gestión de estudiantes del sistema")
public class EstudianteController {

    private final EstudianteService estudianteService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param estudianteService servicio de profesores
     */
    public EstudianteController(final EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    /**
     * Crea un nuevo estudiante.
     *
     * @param estudiante datos del estudiante a crear
     * @return el estudiante creado con HTTP 201
     */
    @PostMapping
    @Operation(summary = "Crear estudiante",
            description = "Registra un nuevo estudiante en el sistema")
    public ResponseEntity<EstudianteModel> crear(
            @RequestBody final EstudianteModel estudiante) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estudianteService.crear(estudiante));
    }

    /**
     * Actualiza un estudiante existente.
     *
     * @param estudiante datos actualizados del estudiante
     * @return el estudiante actualizado con HTTP 200
     */
    @PutMapping
    @Operation(summary = "Actualizar estudiante",
            description = "Actualiza los datos de un estudiante existente")
    public ResponseEntity<EstudianteModel> actualizar(
            @RequestBody final EstudianteModel estudiante) {
        return ResponseEntity.ok(estudianteService.actualizar(estudiante));
    }

    /**
     * Elimina un estudiante por su id.
     *
     * @param id identificador del estudiante a eliminar
     * @return HTTP 204 sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estudiante",
            description = "Elimina un estudiante del sistema por su id")
    public ResponseEntity<Void> eliminar(@PathVariable final Long id) {
        estudianteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna todos los estudiantes.
     *
     * @return lista de estudiantes con HTTP 200
     */
    @GetMapping
    @Operation(summary = "Listar estudiantes",
            description = "Retorna todos los estudiantes registrados")
    public ResponseEntity<List<EstudianteModel>> obtenerTodos() {
        return ResponseEntity.ok(estudianteService.obtenerTodos());
    }

    /**
     * Busca un estudiante por su id.
     *
     * @param id identificador del estudiante
     * @return el estudiante encontrado con HTTP 200
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar por id",
            description = "Retorna un profesor específico por su id")
    public ResponseEntity<EstudianteModel> obtenerPorId(
            @PathVariable final Long id) {
        return ResponseEntity.ok(estudianteService.obtenerPorId(id));
    }

    /**
     * Busca estudiantes por nombre.
     *
     * @param nombre fragmento del nombre a buscar
     * @return lista de estudiantes que coinciden con HTTP 200
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar por nombre",
            description = "Busca estudiantes cuyo nombre contenga el texto indicado")
    public ResponseEntity<List<EstudianteModel>> buscarPorNombre(
            @RequestParam final String nombre) {
        return ResponseEntity.ok(estudianteService.buscarPorNombre(nombre));
    }

    /**
     * Busca estudiantes por semestre.
     *
     * @param semestre fragmento del nombre a buscar
     * @return lista de estudiantes que coinciden con HTTP 200
     */
    @GetMapping("/buscar/semestre")
    @Operation(summary = "Buscar por semestre",
            description = "Busca estudiantes de un semestre especifico")
    public ResponseEntity<List<EstudianteModel>> buscarPorSemestre(
            @RequestParam final Integer semestre) {
        return ResponseEntity.ok(estudianteService.buscarPorSemestre(semestre));
    }
}
