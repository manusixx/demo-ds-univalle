package co.edu.univalle.demo.repository;

import co.edu.univalle.demo.model.CursoModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad CursoModel.
 * Spring Data genera automáticamente la implementación en tiempo de ejecución.
 */
@Repository
public interface CursoRepository extends JpaRepository<CursoModel, Long> {

    /**
     * Busca un curso por su código único.
     * @param codigo código del curso a buscar, ej: DS-101
     * @return el curso si existe
     */
    Optional<CursoModel> findByCodigo(String codigo);

    /**
     * Busca cursos por periodo académico.
     * @param semestre periodo académico, ej: 2026-1
     * @return lista de cursos en ese semestre
     */
    List<CursoModel> findAllBySemestre(String semestre);

    /**
     * Busca cursos asignados a un profesor específico.
     * Spring Data genera el SQL automáticamente navegando
     * la relación @ManyToOne: WHERE cu_profesor_id = profesorId
     * @param profesorId id del profesor
     * @return lista de cursos del profesor
     */
    List<CursoModel> findAllByProfesorId(Long profesorId);

    /**
     * Busca cursos en los que está matriculado un estudiante.
     * Usa JPQL porque Spring Data no puede inferir automáticamente
     * una query que navega la relación inversa de un @ManyToMany.
     * JOIN c.estudiantes navega directamente el grafo de objetos
     * y produce un JOIN limpio contra la tabla curso_estudiante.
     * @param estudianteId id del estudiante
     * @return lista de cursos del estudiante
     */
    @Query("SELECT c FROM CursoModel c JOIN c.estudiantes e WHERE e.id = :estudianteId")
    List<CursoModel> findCursosByEstudianteId(@Param("estudianteId") Long estudianteId);
}