package co.edu.univalle.demo.service;

import co.edu.univalle.demo.exception.BusinessException;
import co.edu.univalle.demo.exception.ResourceNotFoundException;
import co.edu.univalle.demo.model.EstudianteModel;
import co.edu.univalle.demo.repository.EstudianteRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstudianteService {
    private final EstudianteRepository estudianteRepository;

    public EstudianteService(final EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

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

    @Transactional
    public void eliminar(final Long id) {
        estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante con id " + id + " no encontrado"
                ));
        estudianteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EstudianteModel> obtenerTodos() {
        return estudianteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EstudianteModel obtenerPorId(final Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante con id " + id + " no encontrado"
                ));
    }

    @Transactional(readOnly = true)
    public List<EstudianteModel> buscarPorNombre(final String nombre) {
        return estudianteRepository.findAllByNombreContainingIgnoreCase(nombre);
    }

    @Transactional(readOnly = true)
    public Optional<EstudianteModel> buscarPorEmail(final String email) {
        return estudianteRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<EstudianteModel> buscarPorSemestre(final Integer semestre) {
        return estudianteRepository.findAllBySemestre(semestre);
    }
}
