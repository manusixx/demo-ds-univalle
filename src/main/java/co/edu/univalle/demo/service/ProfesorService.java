package co.edu.univalle.demo.service;

import co.edu.univalle.demo.exception.BusinessException;
import co.edu.univalle.demo.exception.ResourceNotFoundException;
import co.edu.univalle.demo.model.ProfesorModel;
import co.edu.univalle.demo.repository.ProfesorRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio que contiene la lógica de negocio para la gestión de profesores. */
@Service
public class ProfesorService {

    /** Repositorio de acceso a datos de profesores. */
    private final ProfesorRepository profesorRepository;

    /**
     * Constructor con inyección de dependencias.
     * @param profesorRepository repositorio de profesores
     */
    public ProfesorService(final ProfesorRepository profesorRepository) {
        this.profesorRepository = profesorRepository;
    }

    /**
     * Crea un nuevo profesor validando unicidad de email e identificación.
     * @param profesor datos del profesor a crear
     * @return el profesor creado con su id asignado
     * @throws BusinessException si el email o identificación ya están registrados
     */
    @Transactional
    public ProfesorModel crear(final ProfesorModel profesor) {
        if (profesorRepository.findByEmail(profesor.getEmail()).isPresent()) {
            throw new BusinessException(
                    "Ya existe un profesor con el email: " + profesor.getEmail()
            );
        }
        if (profesorRepository.findByIdentificacion(
                profesor.getIdentificacion()).isPresent()) {
            throw new BusinessException(
                    "Ya existe un profesor con la identificación: "
                            + profesor.getIdentificacion()
            );
        }
        return profesorRepository.save(profesor);
    }

    /**
     * Actualiza los datos de un profesor existente.
     * @param profesor datos actualizados del profesor con id válido
     * @return el profesor actualizado
     * @throws ResourceNotFoundException si el profesor no existe
     */
    @Transactional
    public ProfesorModel actualizar(final ProfesorModel profesor) {
        profesorRepository.findById(profesor.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesor con id " + profesor.getId() + " no encontrado"
                ));
        return profesorRepository.save(profesor);
    }

    /**
     * Elimina un profesor por su id.
     * @param id identificador del profesor a eliminar
     * @throws ResourceNotFoundException si el profesor no existe
     */
    @Transactional
    public void eliminar(final Long id) {
        profesorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesor con id " + id + " no encontrado"
                ));
        profesorRepository.deleteById(id);
    }

    /**
     * Retorna todos los profesores registrados.
     * @return lista de profesores
     */
    @Transactional(readOnly = true)
    public List<ProfesorModel> obtenerTodos() {
        return profesorRepository.findAll();
    }

    /**
     * Busca un profesor por su id.
     * @param id identificador del profesor
     * @return el profesor encontrado
     * @throws ResourceNotFoundException si el profesor no existe
     */
    @Transactional(readOnly = true)
    public ProfesorModel obtenerPorId(final Long id) {
        return profesorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesor con id " + id + " no encontrado"
                ));
    }

    /**
     * Busca profesores cuyo nombre contenga el texto indicado.
     * @param nombre fragmento del nombre a buscar
     * @return lista de profesores que coinciden
     */
    @Transactional(readOnly = true)
    public List<ProfesorModel> buscarPorNombre(final String nombre) {
        return profesorRepository.findAllByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Busca un profesor por su email.
     * @param email correo electrónico del profesor
     * @return Optional con el profesor si existe
     */
    @Transactional(readOnly = true)
    public Optional<ProfesorModel> buscarPorEmail(final String email) {
        return profesorRepository.findByEmail(email);
    }
}