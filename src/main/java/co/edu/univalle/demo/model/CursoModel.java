package co.edu.univalle.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entidad JPA que representa un curso en el sistema de matrícula.
 * Un curso tiene un profesor asignado y varios estudiantes matriculados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "curso")
public class CursoModel {

    /** Identificador interno autoincremental. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cu_id")
    private Long id;

    /** Nombre descriptivo del curso. */
    @Column(name = "cu_nombre", nullable = false, length = 150)
    private String nombre;

    /** Código único del curso, ej: DS-101. */
    @Column(name = "cu_codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    /** Número de créditos del curso, entre 1 y 6. */
    @Column(name = "cu_creditos", nullable = false)
    private Integer creditos;

    /** Periodo académico del curso, ej: 2026-1. */
    @Column(name = "cu_semestre", nullable = false, length = 10)
    private String semestre;

    /**
     * Profesor que oferta el curso.
     * @ManyToOne: muchos cursos pueden tener el mismo profesor.
     * LAZY: JPA no carga el profesor hasta que se acceda explícitamente —
     * evita JOINs innecesarios cuando solo se necesitan datos básicos del curso.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cu_profesor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProfesorModel profesor;

    /**
     * Estudiantes matriculados en el curso.
     * @ManyToMany: un curso tiene varios estudiantes y un estudiante
     * puede estar en varios cursos. JPA gestiona la tabla intermedia
     * curso_estudiante automáticamente a través de @JoinTable.
     * @ToString.Exclude: OBLIGATORIO — sin esto Lombok genera un toString()
     * que llama recursivamente CursoModel → EstudianteModel → CursoModel
     * produciendo un StackOverflowError en tiempo de ejecución.
     * @Builder.Default: garantiza que la colección se inicialice como
     * HashSet vacío al usar el builder — sin esto getEstudiantes()
     * lanzaría NullPointerException.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "curso_estudiante",
            joinColumns = @JoinColumn(name = "cu_id"),
            inverseJoinColumns = @JoinColumn(name = "es_id")
    )
    @ToString.Exclude
    @Builder.Default
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<EstudianteModel> estudiantes = new HashSet<>();
}