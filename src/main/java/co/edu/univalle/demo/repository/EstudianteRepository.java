package co.edu.univalle.demo.repository;

import co.edu.univalle.demo.model.EstudianteModel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepository extends JpaRepository<EstudianteModel, Long> {
    /**
     * Busca estudiantes cuyo nombre contenga el texto indicado.
     *
     * @param nombre fragmento del nombre a buscar
     * @return lista de estudiantes que coinciden
     */
    List<EstudianteModel> findAllByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca un estudiante por su correo electrónico.
     *
     * @param email correo electrónico a buscar
     * @return el estudiantes si existe
     */
    Optional<EstudianteModel> findByEmail(String email);

    /**
     * Busca un estudiante por su número de identificación.
     * @param identificacion número de documento a buscar
     * @return el estudiante si existe
     */
    Optional<EstudianteModel> findByIdentificacion(String identificacion);

    /**
     * Busca estudiantes por semestre.
     *
     * @param semestre fragmento del nombre a buscar
     * @return lista de estudiantes que coinciden
     */
    List<EstudianteModel> findAllBySemestre(Integer semestre);
}
