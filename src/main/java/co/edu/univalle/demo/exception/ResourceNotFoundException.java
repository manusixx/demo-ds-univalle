package co.edu.univalle.demo.exception;

/** Excepción lanzada cuando un recurso no existe en la base de datos.
 * El GlobalExceptionHandler la mapea a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo.
     * @param message descripción del recurso no encontrado
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }
}