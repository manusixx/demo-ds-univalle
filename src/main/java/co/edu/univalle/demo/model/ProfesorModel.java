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

/** Entidad JPA que representa un profesor en el sistema de matrícula. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profesor")
public class ProfesorModel {

    /** Identificador interno autoincremental. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pr_id")
    private Long id;

    /** Nombre del profesor. */
    @Column(name = "pr_nombre", nullable = false, length = 100)
    private String nombre;

    /** Apellido del profesor. */
    @Column(name = "pr_apellido", nullable = false, length = 100)
    private String apellido;

    /** Correo electrónico único del profesor. */
    @Column(name = "pr_email", nullable = false, unique = true, length = 150)
    private String email;

    /** Número de documento de identidad único. */
    @Column(name = "pr_identificacion", nullable = false, unique = true, length = 20)
    private String identificacion;
}