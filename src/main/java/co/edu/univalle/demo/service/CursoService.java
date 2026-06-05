package co.edu.univalle.demo.service;

import co.edu.univalle.demo.exception.BusinessException;
import co.edu.univalle.demo.exception.ResourceNotFoundException;
import co.edu.univalle.demo.model.CursoModel;
import co.edu.univalle.demo.model.EstudianteModel;
import co.edu.univalle.demo.model.ProfesorModel;
import co.edu.univalle.demo.repository.CursoRepository;
import co.edu.univalle.demo.repository.EstudianteRepository;
import co.edu.univalle.demo.repository.ProfesorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio con la lógica de negocio para gestión de cursos y matrículas.
 * A diferencia de ProfesorService y EstudianteService, este service
 * depende de tres repositorios porque la lógica de matrícula involucra
 * las tres entidades del sistema.
 */
@Service
public class CursoService {

    /** Repositorio de cursos. */
    private final CursoRepository cursoRepository;

    /** Repositorio de profesores — necesario para validar que el profesor existe. */
    private final ProfesorRepository profesorRepository;

    /** Repositorio de estudiantes — necesario para validar que el estudiante existe. */
    private final EstudianteRepository estudianteRepository;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente los tres repositorios.
     * @param cursoRepository repositorio de cursos
     * @param profesorRepository repositorio de profesores
     * @param estudianteRepository repositorio de estudiantes
     */
    public CursoService(
            final CursoRepository cursoRepository,
            final ProfesorRepository profesorRepository,
            final EstudianteRepository estudianteRepository) {
        this.cursoRepository = cursoRepository;
        this.profesorRepository = profesorRepository;
        this.estudianteRepository = estudianteRepository;
    }

    /**
     * Crea un nuevo curso validando que el profesor existe,
     * el código es único y los créditos están en rango válido.
     * @param curso datos del curso a crear
     * @param profesorId id del profesor que oferta el curso
     * @return el curso creado con su id asignado
     * @throws ResourceNotFoundException si el profesor no existe
     * @throws BusinessException si el código ya existe o los créditos son inválidos
     */
    @Transactional
    public CursoModel crear(final CursoModel curso, final Long profesorId) {
        // Primero verificamos que el profesor existe
        // si no existe, no tiene sentido continuar con las demás validaciones
        ProfesorModel profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesor con id " + profesorId + " no encontrado"
                ));

        // El código del curso debe ser único en todo el sistema
        if (cursoRepository.findByCodigo(curso.getCodigo()).isPresent()) {
            throw new BusinessException(
                    "Ya existe un curso con el código: " + curso.getCodigo()
            );
        }

        // Los créditos deben estar entre 1 y 6
        // primera línea de defensa — la BD tiene el CHECK constraint como segunda
        if (curso.getCreditos() < 1 || curso.getCreditos() > 6) {
            throw new BusinessException(
                    "Los créditos deben estar entre 1 y 6"
            );
        }

        // Asignamos el profesor al curso antes de guardar
        // JPA persiste automáticamente la FK cu_profesor_id en la tabla curso
        curso.setProfesor(profesor);
        return cursoRepository.save(curso);
    }

    /**
     * Actualiza los datos básicos de un curso existente.
     * No permite cambiar el profesor ni los estudiantes matriculados
     * — esas operaciones tienen sus propios métodos.
     * @param curso datos actualizados con id válido
     * @return el curso actualizado
     * @throws ResourceNotFoundException si el curso no existe
     * @throws BusinessException si los créditos están fuera del rango válido
     */
    @Transactional
    public CursoModel actualizar(final CursoModel curso) {
        // Verificamos que el curso existe antes de actualizar
        CursoModel existente = cursoRepository.findById(curso.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso con id " + curso.getId() + " no encontrado"
                ));

        // Validamos los créditos también al actualizar
        if (curso.getCreditos() < 1 || curso.getCreditos() > 6) {
            throw new BusinessException(
                    "Los créditos deben estar entre 1 y 6"
            );
        }

        // Actualizamos solo los campos básicos del curso
        // mantenemos el profesor y los estudiantes sin cambios
        existente.setNombre(curso.getNombre());
        existente.setCreditos(curso.getCreditos());
        existente.setSemestre(curso.getSemestre());
        return cursoRepository.save(existente);
    }

    /**
     * Elimina un curso por su id.
     * Las matrículas asociadas se eliminan automáticamente
     * por el ON DELETE CASCADE definido en curso_estudiante.
     * @param id identificador del curso a eliminar
     * @throws ResourceNotFoundException si el curso no existe
     */
    @Transactional
    public void eliminar(final Long id) {
        cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso con id " + id + " no encontrado"
                ));
        cursoRepository.deleteById(id);
    }

    /**
     * Matricula un estudiante en un curso.
     * Verifica que tanto el curso como el estudiante existen,
     * y que el estudiante no está ya matriculado en ese curso.
     * @param cursoId id del curso
     * @param estudianteId id del estudiante a matricular
     * @return el curso actualizado con el estudiante matriculado
     * @throws ResourceNotFoundException si el curso o estudiante no existen
     * @throws BusinessException si el estudiante ya está matriculado
     */
    @Transactional
    public CursoModel matricularEstudiante(
            final Long cursoId, final Long estudianteId) {
        // Cargamos el curso — necesitamos el objeto completo para
        // manipular la colección de estudiantes
        CursoModel curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso con id " + cursoId + " no encontrado"
                ));

        // Cargamos el estudiante — necesitamos el objeto completo
        // para agregarlo al Set de estudiantes del curso
        EstudianteModel estudiante = estudianteRepository
                .findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante con id " + estudianteId + " no encontrado"
                ));

        // Verificamos que el estudiante no está ya matriculado
        // Set.contains() usa equals() — Lombok lo genera en @Data
        // La PK compuesta (cu_id, es_id) en BD también lo garantiza
        if (curso.getEstudiantes().contains(estudiante)) {
            throw new BusinessException(
                    "El estudiante con id " + estudianteId
                            + " ya está matriculado en el curso con id " + cursoId
            );
        }

        // Agregamos el estudiante al Set — JPA persiste automáticamente
        // el registro en la tabla intermedia curso_estudiante
        curso.getEstudiantes().add(estudiante);
        return cursoRepository.save(curso);
    }

    /**
     * Retira un estudiante de un curso.
     * Verifica que el estudiante está efectivamente matriculado
     * antes de intentar retirarlo.
     * @param cursoId id del curso
     * @param estudianteId id del estudiante a retirar
     * @return el curso actualizado sin el estudiante
     * @throws ResourceNotFoundException si el curso o estudiante no existen
     * @throws BusinessException si el estudiante no está matriculado
     */
    @Transactional
    public CursoModel retirarEstudiante(
            final Long cursoId, final Long estudianteId) {
        CursoModel curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso con id " + cursoId + " no encontrado"
                ));

        EstudianteModel estudiante = estudianteRepository
                .findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante con id " + estudianteId + " no encontrado"
                ));

        // Verificamos que el estudiante SÍ está matriculado antes de retirarlo
        if (!curso.getEstudiantes().contains(estudiante)) {
            throw new BusinessException(
                    "El estudiante con id " + estudianteId
                            + " no está matriculado en el curso con id " + cursoId
            );
        }

        // Removemos el estudiante del Set — JPA elimina automáticamente
        // el registro correspondiente en la tabla intermedia curso_estudiante
        curso.getEstudiantes().remove(estudiante);
        return cursoRepository.save(curso);
    }

    /**
     * Retorna todos los cursos registrados.
     * @return lista de cursos
     */
    @Transactional(readOnly = true)
    public List<CursoModel> obtenerTodos() {
        return cursoRepository.findAll();
    }

    /**
     * Busca un curso por su id.
     * @param id identificador del curso
     * @return el curso encontrado
     * @throws ResourceNotFoundException si el curso no existe
     */
    @Transactional(readOnly = true)
    public CursoModel obtenerPorId(final Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso con id " + id + " no encontrado"
                ));
    }

    /**
     * Busca cursos por periodo académico.
     * @param semestre periodo académico a buscar, ej: 2026-1
     * @return lista de cursos en ese semestre
     */
    @Transactional(readOnly = true)
    public List<CursoModel> buscarPorSemestre(final String semestre) {
        return cursoRepository.findAllBySemestre(semestre);
    }

    /**
     * Busca cursos asignados a un profesor.
     * @param profesorId id del profesor
     * @return lista de cursos del profesor
     */
    @Transactional(readOnly = true)
    public List<CursoModel> buscarPorProfesor(final Long profesorId) {
        return cursoRepository.findAllByProfesorId(profesorId);
    }

    /**
     * Busca cursos en los que está matriculado un estudiante.
     * @param estudianteId id del estudiante
     * @return lista de cursos del estudiante
     */
    @Transactional(readOnly = true)
    public List<CursoModel> buscarPorEstudiante(final Long estudianteId) {
        return cursoRepository.findCursosByEstudianteId(estudianteId);
    }
}