package co.edu.univalle.demo.repository;

import co.edu.univalle.demo.model.ProfesorModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio JPA para la entidad ProfesorModel.
 * Spring Data genera automáticamente la implementación en tiempo de ejecución.
 */
@Repository
public interface ProfesorRepository extends JpaRepository<ProfesorModel, Long> {

    /**
     * Busca profesores cuyo nombre contenga el texto indicado.
     * @param nombre fragmento del nombre a buscar
     * @return lista de profesores que coinciden
     */
    List<ProfesorModel> findAllByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca un profesor por su correo electrónico.
     * @param email correo electrónico a buscar
     * @return el profesor si existe
     */
    Optional<ProfesorModel> findByEmail(String email);

    /**
     * Busca un profesor por su número de identificación.
     * @param identificacion número de documento a buscar
     * @return el profesor si existe
     */
    Optional<ProfesorModel> findByIdentificacion(String identificacion);
}