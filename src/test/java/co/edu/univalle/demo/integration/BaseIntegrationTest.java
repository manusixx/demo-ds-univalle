package co.edu.univalle.demo.integration;

// @SpringBootTest levanta el contexto completo de Spring para los tests de integración
// RANDOM_PORT asigna un puerto aleatorio disponible al servidor HTTP
// evitando conflictos si hay otra aplicación corriendo en el puerto 8085
import org.springframework.boot.test.context.SpringBootTest;

// @Testcontainers activa la integración de JUnit 5 con Testcontainers
// le indica a JUnit que debe gestionar el ciclo de vida de los contenedores Docker
import org.testcontainers.junit.jupiter.Testcontainers;

// PostgreSQLContainer es el contenedor Docker de PostgreSQL que Testcontainers levanta
// internamente hace un docker pull postgres:16-alpine y arranca el contenedor
import org.testcontainers.containers.PostgreSQLContainer;

// DynamicPropertyRegistry permite inyectar propiedades dinámicas en el contexto de Spring
// las usamos para sobreescribir la URL de BD con la del contenedor en tiempo de ejecución
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

// La clase es abstract porque no contiene tests propios
// actúa como clase base que todas las clases de integración heredan
// evita repetir la configuración de Testcontainers en cada clase de test
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    // El contenedor es static final — esto es CRÍTICO
    // static: pertenece a la clase, no a cada instancia
    // final: no puede ser reasignado
    // Combinados garantizan que existe UN SOLO contenedor compartido
    // entre ProfesorIntegrationTest, EstudianteIntegrationTest y CursoIntegrationTest
    // sin esto cada clase levanta su propio contenedor y el anterior muere
    // cuando Spring crea un nuevo contexto — causando el error de conexión rechazada
    static final PostgreSQLContainer<?> postgres;

    // Bloque estático — se ejecuta UNA SOLA VEZ cuando la JVM carga la clase
    // iniciamos el contenedor manualmente con .start() en lugar de @Container
    // porque @Container destruye el contenedor al finalizar cada clase de test
    // con el bloque estático el contenedor vive durante toda la suite
    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                // nombre de la base de datos que se crea dentro del contenedor
                .withDatabaseName("matricula_test")
                // usuario con permisos sobre esa base de datos
                .withUsername("test_user")
                // contraseña del usuario
                .withPassword("test_pass");
        // Iniciamos el contenedor manualmente — JUnit no lo gestiona
        // esto garantiza que el contenedor persiste entre clases de test
        // Testcontainers descarga la imagen postgres:16-alpine la primera vez
        // y la reutiliza desde el cache de Docker en ejecuciones posteriores
        postgres.start();
    }

    // @DynamicPropertySource se ejecuta ANTES de que Spring arranque
    // sobreescribe las propiedades del application.yml con los valores reales
    // del contenedor que Testcontainers acaba de levantar
    // esto es necesario porque Testcontainers asigna un puerto aleatorio
    // al contenedor cada vez — no podemos hardcodear la URL de conexión
    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {

        // postgres::getJdbcUrl devuelve algo como:
        // jdbc:postgresql://localhost:49152/matricula_test
        // donde 49152 es el puerto aleatorio asignado por Testcontainers
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Flyway también necesita su propia URL de conexión para ejecutar
        // las migraciones V1__, V2__, V3__ sobre el contenedor de test
        // sin esto Flyway intentaría conectarse a la BD del application-dev.yml
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }
}