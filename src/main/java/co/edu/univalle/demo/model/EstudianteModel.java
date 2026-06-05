package co.edu.univalle.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa un profesor en el sistema de matrícula.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estudiante")
public class EstudianteModel {

    /**
     * Identificador interno autoincremental.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "es_id")
    private Long id;

    /**
     * Nombre del estudiante.
     */
    @Column(name = "es_nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Apellido del estudiante.
     */
    @Column(name = "es_apellido", nullable = false, length = 100)
    private String apellido;

    /**
     * Correo electrónico único del estudiante.
     */
    @Column(name = "es_email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Número de documento de identidad único.
     */
    @Column(name = "es_identificacion", nullable = false, unique = true, length = 20)
    private String identificacion;

    @Column(name = "es_semestre", nullable = false)
    private Integer semestre;

}
