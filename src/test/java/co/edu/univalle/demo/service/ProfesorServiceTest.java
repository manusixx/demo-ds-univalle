package co.edu.univalle.demo.service;

// Importamos las excepciones propias del proyecto que el service puede lanzar
import co.edu.univalle.demo.exception.BusinessException;
import co.edu.univalle.demo.exception.ResourceNotFoundException;

// Importamos el model (entidad JPA) y el repository que vamos a simular con Mockito
import co.edu.univalle.demo.model.ProfesorModel;
import co.edu.univalle.demo.repository.ProfesorRepository;

import java.util.List;
import java.util.Optional;

// JUnit 5 — framework de pruebas. Provee @Test, @BeforeEach, @DisplayName
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

// Mockito — librería para crear objetos simulados (mocks)
import org.mockito.Mock;

// Integración entre Spring y JUnit 5
import org.springframework.test.context.junit.jupiter.SpringExtension;

// Assertions — métodos para verificar que el resultado es el esperado
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// BDDMockito — permite escribir los mocks en estilo "dado que... cuando... entonces..."
import static org.mockito.BDDMockito.given;

// Mockito — permite verificar que un método fue o NO fue llamado
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// @ExtendWith conecta JUnit 5 con Spring para que Mockito funcione correctamente
// Sin esta anotación los @Mock no se inicializan y los tests fallan
@ExtendWith(SpringExtension.class)
class ProfesorServiceTest {

    // @Mock crea un objeto simulado de ProfesorRepository
    // Este mock NO accede a la base de datos — devuelve lo que nosotros le indiquemos
    // El objetivo es aislar el Service de cualquier dependencia externa
    @Mock
    private ProfesorRepository profesorRepository;

    // El Service REAL que vamos a probar — no es un mock, es la clase real
    private ProfesorService profesorService;

    // @BeforeEach se ejecuta ANTES de cada test
    // Crea una instancia nueva del Service inyectándole el repositorio simulado
    // Así cada test parte de un estado limpio e independiente
    @BeforeEach
    void setUp() {
        profesorService = new ProfesorService(profesorRepository);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    // Método auxiliar que construye un ProfesorModel de prueba
    // Lo usamos en varios tests para no repetir el mismo código de construcción
    private ProfesorModel buildProfesor() {
        return ProfesorModel.builder()
                .id(1L)
                .nombre("Carlos")
                .apellido("Ramírez")
                .email("carlos@univalle.edu.co")
                .identificacion("12345678")
                .build();
    }

    // ── Crear ─────────────────────────────────────────────────────────────────

    // Escenario feliz: todos los datos son únicos, el profesor se crea correctamente
    @Test
    @DisplayName("Crear profesor exitosamente cuando email e identificación son únicos")
    void whenCreateProfesor_withUniqueData_thenReturnSavedProfesor() {
        var profesor = buildProfesor();

        // "Dado que" el repositorio no encuentra ningún profesor con ese email...
        given(profesorRepository.findByEmail(profesor.getEmail()))
                .willReturn(Optional.empty());

        // "Dado que" el repositorio no encuentra ningún profesor con esa identificación...
        given(profesorRepository.findByIdentificacion(profesor.getIdentificacion()))
                .willReturn(Optional.empty());

        // "Dado que" al guardar el repositorio devuelve el profesor guardado...
        given(profesorRepository.save(profesor)).willReturn(profesor);

        // Ejecutamos el método real del Service que queremos probar
        ProfesorModel result = profesorService.crear(profesor);

        // Verificamos que el repositorio SÍ llamó a save() — se guardó en BD
        verify(profesorRepository).save(profesor);

        // Verificamos que el resultado devuelto es el profesor esperado
        assertEquals(profesor, result);
    }

    // Escenario de error: el email ya existe — debe lanzar BusinessException
    // verify(never) confirma que NO se intentó guardar en BD al haber error
    @Test
    @DisplayName("Lanzar BusinessException al crear profesor con email duplicado")
    void whenCreateProfesor_withDuplicateEmail_thenThrowBusinessException() {
        var profesor = buildProfesor();

        // Simulamos que YA existe un profesor con ese email en la BD
        given(profesorRepository.findByEmail(profesor.getEmail()))
                .willReturn(Optional.of(profesor));

        // Verificamos que el Service lanza BusinessException ante email duplicado
        assertThrows(BusinessException.class,
                () -> profesorService.crear(profesor));

        // Verificamos que save() NUNCA fue llamado — no se intentó guardar
        verify(profesorRepository, never()).save(profesor);
    }

    // Escenario de error: la identificación ya existe — debe lanzar BusinessException
    @Test
    @DisplayName("Lanzar BusinessException al crear profesor con identificación duplicada")
    void whenCreateProfesor_withDuplicateIdentificacion_thenThrowBusinessException() {
        var profesor = buildProfesor();

        // El email no existe — pasa la primera validación
        given(profesorRepository.findByEmail(profesor.getEmail()))
                .willReturn(Optional.empty());

        // Pero la identificación YA existe — falla en la segunda validación
        given(profesorRepository.findByIdentificacion(profesor.getIdentificacion()))
                .willReturn(Optional.of(profesor));

        assertThrows(BusinessException.class,
                () -> profesorService.crear(profesor));
        verify(profesorRepository, never()).save(profesor);
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    // Escenario feliz: el profesor existe, se actualiza correctamente
    @Test
    @DisplayName("Actualizar profesor exitosamente cuando existe")
    void whenUpdateProfesor_withExistingId_thenReturnUpdatedProfesor() {
        var profesor = buildProfesor();

        // Simulamos que el profesor SÍ existe en la BD
        given(profesorRepository.findById(profesor.getId()))
                .willReturn(Optional.of(profesor));
        given(profesorRepository.save(profesor)).willReturn(profesor);

        ProfesorModel result = profesorService.actualizar(profesor);

        verify(profesorRepository).save(profesor);
        assertEquals(profesor, result);
    }

    // Escenario de error: el profesor no existe — debe lanzar ResourceNotFoundException
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al actualizar profesor inexistente")
    void whenUpdateProfesor_withNonExistingId_thenThrowResourceNotFoundException() {
        var profesor = buildProfesor();

        // Simulamos que el profesor NO existe en la BD
        given(profesorRepository.findById(profesor.getId()))
                .willReturn(Optional.empty());

        // El Service debe lanzar ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class,
                () -> profesorService.actualizar(profesor));

        // Si no existe, save() no debe ser llamado nunca
        verify(profesorRepository, never()).save(profesor);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    // Escenario feliz: el profesor existe, se elimina correctamente
    @Test
    @DisplayName("Eliminar profesor exitosamente cuando existe")
    void whenDeleteProfesor_withExistingId_thenDeleteFromRepository() {
        var profesor = buildProfesor();

        // Simulamos que el profesor SÍ existe
        given(profesorRepository.findById(profesor.getId()))
                .willReturn(Optional.of(profesor));

        profesorService.eliminar(profesor.getId());

        // Verificamos que deleteById() fue llamado con el id correcto
        verify(profesorRepository).deleteById(profesor.getId());
    }

    // Escenario de error: el profesor no existe — debe lanzar ResourceNotFoundException
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al eliminar profesor inexistente")
    void whenDeleteProfesor_withNonExistingId_thenThrowResourceNotFoundException() {
        given(profesorRepository.findById(99L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profesorService.eliminar(99L));

        // Si no existe, deleteById() no debe ser llamado nunca
        verify(profesorRepository, never()).deleteById(99L);
    }

    // ── Obtener todos ─────────────────────────────────────────────────────────

    // Verifica que obtenerTodos() delega correctamente al repositorio
    // y devuelve exactamente lo que el repositorio retorna
    @Test
    @DisplayName("Retornar lista de todos los profesores")
    void whenGetAllProfesores_thenReturnList() {
        var profesores = List.of(buildProfesor());

        // Simulamos que la BD tiene un profesor
        given(profesorRepository.findAll()).willReturn(profesores);

        List<ProfesorModel> result = profesorService.obtenerTodos();

        // Verificamos que findAll() fue llamado
        verify(profesorRepository).findAll();

        // Verificamos que la lista devuelta es la correcta
        assertEquals(profesores, result);
    }

    // ── Obtener por id ────────────────────────────────────────────────────────

    // Escenario feliz: el profesor existe, se retorna correctamente
    @Test
    @DisplayName("Retornar profesor por id cuando existe")
    void whenGetProfesorById_withExistingId_thenReturnProfesor() {
        var profesor = buildProfesor();
        given(profesorRepository.findById(profesor.getId()))
                .willReturn(Optional.of(profesor));

        ProfesorModel result = profesorService.obtenerPorId(profesor.getId());

        assertEquals(profesor, result);
    }

    // Escenario de error: id no existe — debe lanzar ResourceNotFoundException
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al buscar id inexistente")
    void whenGetProfesorById_withNonExistingId_thenThrowResourceNotFoundException() {
        given(profesorRepository.findById(99L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profesorService.obtenerPorId(99L));
    }

    // ── Buscar por nombre ─────────────────────────────────────────────────────

    // Verifica que la búsqueda por nombre delega al repositorio
    // y devuelve los resultados correctamente
    @Test
    @DisplayName("Retornar profesores que coinciden con el nombre buscado")
    void whenSearchByNombre_thenReturnMatchingProfesores() {
        var profesores = List.of(buildProfesor());
        given(profesorRepository.findAllByNombreContainingIgnoreCase("Carlos"))
                .willReturn(profesores);

        List<ProfesorModel> result = profesorService.buscarPorNombre("Carlos");

        assertEquals(profesores, result);
    }

    // ── Buscar por email ──────────────────────────────────────────────────────

    // Escenario feliz: el email existe, retorna el profesor dentro de un Optional
    @Test
    @DisplayName("Retornar profesor cuando el email existe")
    void whenSearchByEmail_withExistingEmail_thenReturnProfesor() {
        var profesor = buildProfesor();
        given(profesorRepository.findByEmail(profesor.getEmail()))
                .willReturn(Optional.of(profesor));

        Optional<ProfesorModel> result =
                profesorService.buscarPorEmail(profesor.getEmail());

        // assertTrue verifica que el Optional no está vacío
        assertTrue(result.isPresent());
        assertEquals(profesor, result.get());
    }

    // Escenario: el email no existe — el Service retorna Optional vacío
    // Esto no lanza excepción — es un resultado válido que el controller maneja
    @Test
    @DisplayName("Retornar Optional vacío cuando el email no existe")
    void whenSearchByEmail_withNonExistingEmail_thenReturnEmpty() {
        given(profesorRepository.findByEmail("noexiste@univalle.edu.co"))
                .willReturn(Optional.empty());

        Optional<ProfesorModel> result =
                profesorService.buscarPorEmail("noexiste@univalle.edu.co");

        // assertTrue verifica que el Optional SÍ está vacío
        assertTrue(result.isEmpty());
    }
}