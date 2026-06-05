package co.edu.univalle.demo.exception;

/** Excepción lanzada cuando se viola una regla de negocio.
 * El GlobalExceptionHandler la mapea a HTTP 409 Conflict.
 */
public class BusinessException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo.
     * @param message descripción de la regla de negocio violada
     */
    public BusinessException(final String message) {
        super(message);
    }
}