package co.edu.univalle.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import co.edu.univalle.demo.integration.BaseIntegrationTest;

/** Test de carga del contexto de Spring Boot. */
@SpringBootTest
class DemoDsUnivalleApplicationTests extends BaseIntegrationTest  {

    /** Verifica que el contexto de Spring arranca correctamente. */
    @Test
    void contextLoads() {
    }
}