package co.edu.univalle.demo.controller;

import co.edu.univalle.demo.model.ProfesorModel;
import co.edu.univalle.demo.service.ProfesorService;
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

/** Controlador REST para la gestión de profesores. */
@RestController
@RequestMapping("/api/v1/profesores")
@Tag(name = "Profesores", description = "Gestión de profesores del sistema")
public class ProfesorController {

    /** Servicio de lógica de negocio para profesores. */
    private final ProfesorService profesorService;

    /**
     * Constructor con inyección de dependencias.
     * @param profesorService servicio de profesores
     */
    public ProfesorController(final ProfesorService profesorService) {
        this.profesorService = profesorService;
    }

    /**
     * Crea un nuevo profesor.
     * @param profesor datos del profesor a crear
     * @return el profesor creado con HTTP 201
     */
    @PostMapping
    @Operation(summary = "Crear profesor",
            description = "Registra un nuevo profesor en el sistema")
    public ResponseEntity<ProfesorModel> crear(
            @RequestBody final ProfesorModel profesor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profesorService.crear(profesor));
    }

    /**
     * Actualiza un profesor existente.
     * @param profesor datos actualizados del profesor
     * @return el profesor actualizado con HTTP 200
     */
    @PutMapping
    @Operation(summary = "Actualizar profesor",
            description = "Actualiza los datos de un profesor existente")
    public ResponseEntity<ProfesorModel> actualizar(
            @RequestBody final ProfesorModel profesor) {
        return ResponseEntity.ok(profesorService.actualizar(profesor));
    }

    /**
     * Elimina un profesor por su id.
     * @param id identificador del profesor a eliminar
     * @return HTTP 204 sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar profesor",
            description = "Elimina un profesor del sistema por su id")
    public ResponseEntity<Void> eliminar(@PathVariable final Long id) {
        profesorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna todos los profesores.
     * @return lista de profesores con HTTP 200
     */
    @GetMapping
    @Operation(summary = "Listar profesores",
            description = "Retorna todos los profesores registrados")
    public ResponseEntity<List<ProfesorModel>> obtenerTodos() {
        return ResponseEntity.ok(profesorService.obtenerTodos());
    }

    /**
     * Busca un profesor por su id.
     * @param id identificador del profesor
     * @return el profesor encontrado con HTTP 200
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar por id",
            description = "Retorna un profesor específico por su id")
    public ResponseEntity<ProfesorModel> obtenerPorId(
            @PathVariable final Long id) {
        return ResponseEntity.ok(profesorService.obtenerPorId(id));
    }

    /**
     * Busca profesores por nombre.
     * @param nombre fragmento del nombre a buscar
     * @return lista de profesores que coinciden con HTTP 200
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar por nombre",
            description = "Busca profesores cuyo nombre contenga el texto indicado")
    public ResponseEntity<List<ProfesorModel>> buscarPorNombre(
            @RequestParam final String nombre) {
        return ResponseEntity.ok(profesorService.buscarPorNombre(nombre));
    }
}