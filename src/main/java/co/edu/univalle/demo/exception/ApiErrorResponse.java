package co.edu.univalle.demo.exception;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Estructura estándar de respuesta de error para toda la API. */
@Getter
@Builder
@AllArgsConstructor
public class ApiErrorResponse {

    /** Código HTTP del error. */
    private int status;

    /** Mensaje descriptivo del error. */
    private String message;

    /** Ruta del endpoint que generó el error. */
    private String path;

    /** Marca de tiempo del momento en que ocurrió el error. */
    private LocalDateTime timestamp;
}