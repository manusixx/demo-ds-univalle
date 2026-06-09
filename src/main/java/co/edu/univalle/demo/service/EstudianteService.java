package co.edu.univalle.demo.service;

import co.edu.univalle.demo.exception.BusinessException;
import co.edu.univalle.demo.exception.ResourceNotFoundException;
import co.edu.univalle.demo.model.EstudianteModel;
import co.edu.univalle.demo.repository.EstudianteRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio con la lógica de negocio para la gestión de estudiantes. */
@Service
public class EstudianteService {

    /** Repositorio de acceso a datos de estudiantes. */
    private final EstudianteRepository estudianteRepository;

    /**
     * Constructor con inyección de dependencias.
     * @param estudianteRepository repositorio de estudiantes
     */
    public EstudianteService(final EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    /**
     * Crea un nuevo estudiante validando unicidad y semestre válido.
     * @param estudiante datos del estudiante a crear
     * @return el estudiante creado con su id asignado
     * @throws BusinessException si el email, identificación están duplicados
     *     o el semestre está fuera del rango 1-10
     */
    @Transactional
    public EstudianteModel crear(final EstudianteModel estudiante) {
        if (estudianteRepository.findByEmail(estudiante.getEmail()).isPresent()) {
            throw new BusinessException(
                    "Ya existe un estudiante con el email: " + estudiante.getEmail()
            );
        }
        if (estudianteRepository.findByIdentificacion(
                estudiante.getIdentificacion()).isPresent()) {
            throw new BusinessException(
                    "Ya existe un estudiante con la identificación: "
                            + estudiante.getIdentificacion()
            );
        }
        // Validamos que el semestre esté en el rango permitido (1 a 10)
        // esta validación es la primera línea de defensa — la BD también tiene
        // un CHECK constraint que rechaza valores fuera del rango
        if (estudiante.getSemestre() < 1 || estudiante.getSemestre() > 10) {
            throw new BusinessException("El semestre debe estar entre 1 y 10");
        }
        return estudianteRepository.save(estudiante);
    }

    /**
     * Actualiza los datos de un estudiante existente.
     * @param estudiante datos actualizados con id válido
     * @return el estudiante actualizado
     * @throws ResourceNotFoundException si el estudiante no existe
     * @throws BusinessException si el semestre está fuera del rango 1-10
     */
    @Transactional
    public EstudianteModel actualizar(final EstudianteModel estudiante) {
        estudianteRepository.findById(estudiante.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante con id " + estudiante.getId() + " no encontrado"
                ));
        // Al actualizar también validamos el semestre
        // un estudiante no puede ser actualizado con un semestre inválido
        if (estudiante.getSemestre() < 1 || estudiante.getSemestre() > 10) {
            throw new BusinessException("El semestre debe estar entre 1 y 10");
        }
        return estudianteRepository.save(estudiante);
    }

    /**
     * Elimina un estudiante por su id.
     * @param id identificador del estudiante a eliminar
     * @throws ResourceNotFoundException si el estudiante no existe
     */
    @Transactional
    public void eliminar(final Long id) {
        estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante con id " + id + " no encontrado"
                ));
        estudianteRepository.deleteById(id);
    }

    /**
     * Retorna todos los estudiantes registrados.
     * @return lista de estudiantes
     */
    @Transactional(readOnly = true)
    public List<EstudianteModel> obtenerTodos() {
        return estudianteRepository.findAll();
    }

    /**
     * Busca un estudiante por su id.
     * @param id identificador del estudiante
     * @return el estudiante encontrado
     * @throws ResourceNotFoundException si el estudiante no existe
     */
    @Transactional(readOnly = true)
    public EstudianteModel obtenerPorId(final Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante con id " + id + " no encontrado"
                ));
    }

    /**
     * Busca estudiantes cuyo nombre contenga el texto indicado.
     * @param nombre fragmento del nombre a buscar
     * @return lista de estudiantes que coinciden
     */
    @Transactional(readOnly = true)
    public List<EstudianteModel> buscarPorNombre(final String nombre) {
        return estudianteRepository.findAllByNombreContainingIgnoreCase(nombre);
    }
    /**
     * Busca un estudiante por su email.
     * @param email correo electrónico del estudiante
     * @return Optional con el estudiante si existe
     */
    @Transactional(readOnly = true)
    public Optional<EstudianteModel> buscarPorEmail(final String email) {
        return estudianteRepository.findByEmail(email);
    }

    /**
     * Busca estudiantes por semestre.
     * @param semestre semestre a buscar (1-10)
     * @return lista de estudiantes en ese semestre
     */
    @Transactional(readOnly = true)
    public List<EstudianteModel> buscarPorSemestre(final Integer semestre) {
        return estudianteRepository.findAllBySemestre(semestre);
    }
}
