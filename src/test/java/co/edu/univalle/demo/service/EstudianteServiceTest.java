package co.edu.univalle.demo.service;

import co.edu.univalle.demo.exception.BusinessException;
import co.edu.univalle.demo.exception.ResourceNotFoundException;
import co.edu.univalle.demo.model.EstudianteModel;
import co.edu.univalle.demo.repository.EstudianteRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// Conecta JUnit 5 con Spring para que los @Mock se inicialicen correctamente
@ExtendWith(SpringExtension.class)
class EstudianteServiceTest {

    // Repositorio simulado — no accede a la BD real
    @Mock
    private EstudianteRepository estudianteRepository;

    // El Service REAL que vamos a probar
    private EstudianteService estudianteService;

    // Se ejecuta antes de cada test — crea una instancia limpia del service
    @BeforeEach
    void setUp() {
        estudianteService = new EstudianteService(estudianteRepository);
    }

    // Helper
    // Construye un EstudianteModel válido para reutilizar en los tests
    private EstudianteModel buildEstudiante() {
        return EstudianteModel.builder()
                .id(1L)
                .nombre("Ana")
                .apellido("García")
                .email("ana@univalle.edu.co")
                .identificacion("87654321")
                .semestre(4)
                .build();
    }

    // Crear

    // Escenario feliz: todos los datos son válidos — se crea correctamente
    @Test
    @DisplayName("Crear estudiante exitosamente con datos únicos y semestre válido")
    void whenCreateEstudiante_withValidData_thenReturnSaved() {
        var estudiante = buildEstudiante();

        // El email no existe en BD
        given(estudianteRepository.findByEmail(estudiante.getEmail()))
                .willReturn(Optional.empty());

        // La identificación no existe en BD
        given(estudianteRepository.findByIdentificacion(
                estudiante.getIdentificacion()))
                .willReturn(Optional.empty());

        // Al guardar devuelve el estudiante con id asignado
        given(estudianteRepository.save(estudiante)).willReturn(estudiante);

        EstudianteModel result = estudianteService.crear(estudiante);

        verify(estudianteRepository).save(estudiante);
        assertEquals(estudiante, result);
    }

    // Escenario de error: email duplicado — no debe guardar
    @Test
    @DisplayName("Lanzar BusinessException al crear con email duplicado")
    void whenCreateEstudiante_withDuplicateEmail_thenThrowBusinessException() {
        var estudiante = buildEstudiante();

        // Simulamos que ya existe un estudiante con ese email
        given(estudianteRepository.findByEmail(estudiante.getEmail()))
                .willReturn(Optional.of(estudiante));

        assertThrows(BusinessException.class,
                () -> estudianteService.crear(estudiante));

        // Si el email ya existe, save() nunca debe llamarse
        verify(estudianteRepository, never()).save(estudiante);
    }

    // Escenario de error: identificación duplicada — no debe guardar
    @Test
    @DisplayName("Lanzar BusinessException al crear con identificación duplicada")
    void whenCreateEstudiante_withDuplicateIdentificacion_thenThrowBusinessException() {
        var estudiante = buildEstudiante();

        // El email pasa la primera validación
        given(estudianteRepository.findByEmail(estudiante.getEmail()))
                .willReturn(Optional.empty());

        // Pero la identificación ya existe — falla en la segunda validación
        given(estudianteRepository.findByIdentificacion(
                estudiante.getIdentificacion()))
                .willReturn(Optional.of(estudiante));

        assertThrows(BusinessException.class,
                () -> estudianteService.crear(estudiante));
        verify(estudianteRepository, never()).save(estudiante);
    }

    // Escenario de error: semestre mayor a 10 — caso de prueba adicional del Paso 8
    // verifica que la validación de semestre funciona en el límite superior
    @Test
    @DisplayName("Lanzar BusinessException al crear con semestre mayor a 10")
    void whenCreateEstudiante_withSemestreMayorDiez_thenThrowBusinessException() {
        // Construimos un estudiante con semestre inválido = 11
        var estudiante = EstudianteModel.builder()
                .id(1L)
                .nombre("Ana")
                .apellido("García")
                .email("ana@univalle.edu.co")
                .identificacion("87654321")
                .semestre(11)   // ← fuera del rango permitido (1-10)
                .build();

        // El email y la identificación pasan las validaciones previas
        given(estudianteRepository.findByEmail(estudiante.getEmail()))
                .willReturn(Optional.empty());
        given(estudianteRepository.findByIdentificacion(
                estudiante.getIdentificacion()))
                .willReturn(Optional.empty());

        // El service debe lanzar BusinessException por semestre inválido
        assertThrows(BusinessException.class,
                () -> estudianteService.crear(estudiante));

        // Si el semestre es inválido, save() nunca debe llamarse
        verify(estudianteRepository, never()).save(estudiante);
    }

    // Escenario de error: semestre menor a 1 — verifica el límite inferior
    @Test
    @DisplayName("Lanzar BusinessException al crear con semestre menor a 1")
    void whenCreateEstudiante_withSemestreMenorUno_thenThrowBusinessException() {
        // Construimos un estudiante con semestre = 0
        var estudiante = EstudianteModel.builder()
                .id(1L)
                .nombre("Ana")
                .apellido("García")
                .email("ana@univalle.edu.co")
                .identificacion("87654321")
                .semestre(0)    // ← por debajo del mínimo permitido
                .build();

        given(estudianteRepository.findByEmail(estudiante.getEmail()))
                .willReturn(Optional.empty());
        given(estudianteRepository.findByIdentificacion(
                estudiante.getIdentificacion()))
                .willReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> estudianteService.crear(estudiante));
        verify(estudianteRepository, never()).save(estudiante);
    }

    // Actualizar

    // Escenario feliz: el estudiante existe y los datos son válidos
    @Test
    @DisplayName("Actualizar estudiante exitosamente cuando existe")
    void whenUpdateEstudiante_withExistingId_thenReturnUpdated() {
        var estudiante = buildEstudiante();

        // Simulamos que el estudiante SÍ existe en BD
        given(estudianteRepository.findById(estudiante.getId()))
                .willReturn(Optional.of(estudiante));
        given(estudianteRepository.save(estudiante)).willReturn(estudiante);

        EstudianteModel result = estudianteService.actualizar(estudiante);

        verify(estudianteRepository).save(estudiante);
        assertEquals(estudiante, result);
    }

    // Escenario de error: el estudiante no existe — debe lanzar ResourceNotFoundException
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al actualizar estudiante inexistente")
    void whenUpdateEstudiante_withNonExistingId_thenThrowResourceNotFoundException() {
        var estudiante = buildEstudiante();

        // Simulamos que el estudiante NO existe en BD
        given(estudianteRepository.findById(estudiante.getId()))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> estudianteService.actualizar(estudiante));
        verify(estudianteRepository, never()).save(estudiante);
    }

    // Escenario de error: semestre inválido al actualizar — caso de prueba adicional
    // el estudiante existe pero el semestre nuevo está fuera del rango
    @Test
    @DisplayName("Lanzar BusinessException al actualizar con semestre inválido")
    void whenUpdateEstudiante_withInvalidSemestre_thenThrowBusinessException() {
        var estudiante = buildEstudiante();

        // El estudiante existe en BD
        given(estudianteRepository.findById(estudiante.getId()))
                .willReturn(Optional.of(estudiante));

        // Modificamos el semestre a un valor inválido después de encontrar el estudiante
        estudiante.setSemestre(11);

        // El service debe lanzar BusinessException por semestre inválido
        assertThrows(BusinessException.class,
                () -> estudianteService.actualizar(estudiante));

        // Con semestre inválido, save() nunca debe llamarse
        verify(estudianteRepository, never()).save(estudiante);
    }

    // Eliminar

    // Escenario feliz: el estudiante existe, se elimina correctamente
    @Test
    @DisplayName("Eliminar estudiante exitosamente cuando existe")
    void whenDeleteEstudiante_withExistingId_thenDeleteFromRepository() {
        var estudiante = buildEstudiante();

        given(estudianteRepository.findById(estudiante.getId()))
                .willReturn(Optional.of(estudiante));

        estudianteService.eliminar(estudiante.getId());

        // Verificamos que deleteById() fue llamado con el id correcto
        verify(estudianteRepository).deleteById(estudiante.getId());
    }

    // Escenario de error: el estudiante no existe — no debe intentar eliminar
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al eliminar estudiante inexistente")
    void whenDeleteEstudiante_withNonExistingId_thenThrowResourceNotFoundException() {
        given(estudianteRepository.findById(99L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> estudianteService.eliminar(99L));

        // Si no existe, deleteById() no debe llamarse nunca
        verify(estudianteRepository, never()).deleteById(99L);
    }

    // ── Obtener todos ─────────────────────────────────────────────────────────

    // Verifica que obtenerTodos() delega al repositorio y retorna la lista correcta
    @Test
    @DisplayName("Retornar lista de todos los estudiantes")
    void whenGetAllEstudiantes_thenReturnList() {
        var estudiantes = List.of(buildEstudiante());
        given(estudianteRepository.findAll()).willReturn(estudiantes);

        List<EstudianteModel> result = estudianteService.obtenerTodos();

        verify(estudianteRepository).findAll();
        assertEquals(estudiantes, result);
    }

    // Obtener por id

    // Escenario feliz: el estudiante existe, se retorna correctamente
    @Test
    @DisplayName("Retornar estudiante por id cuando existe")
    void whenGetEstudianteById_withExistingId_thenReturnEstudiante() {
        var estudiante = buildEstudiante();
        given(estudianteRepository.findById(estudiante.getId()))
                .willReturn(Optional.of(estudiante));

        EstudianteModel result =
                estudianteService.obtenerPorId(estudiante.getId());

        assertEquals(estudiante, result);
    }

    // Escenario de error: id no existe — debe lanzar ResourceNotFoundException
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al buscar id inexistente")
    void whenGetEstudianteById_withNonExistingId_thenThrowResourceNotFoundException() {
        given(estudianteRepository.findById(99L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> estudianteService.obtenerPorId(99L));
    }

    // Buscar por nombre

    // Verifica que la búsqueda por nombre delega al repositorio correctamente
    @Test
    @DisplayName("Retornar estudiantes que coinciden con el nombre buscado")
    void whenSearchByNombre_thenReturnMatchingEstudiantes() {
        var estudiantes = List.of(buildEstudiante());
        given(estudianteRepository.findAllByNombreContainingIgnoreCase("Ana"))
                .willReturn(estudiantes);

        List<EstudianteModel> result =
                estudianteService.buscarPorNombre("Ana");

        assertEquals(estudiantes, result);
    }

    // Buscar por semestre

    // Caso de prueba adicional del Paso 8 — verifica que la búsqueda por semestre
    // delega al repositorio y retorna los estudiantes del semestre indicado
    @Test
    @DisplayName("Retornar estudiantes del semestre indicado")
    void whenGetBySemestre_thenReturnMatchingEstudiantes() {
        // Simulamos que hay un estudiante en semestre 4
        var estudiantes = List.of(buildEstudiante());
        given(estudianteRepository.findAllBySemestre(4))
                .willReturn(estudiantes);

        List<EstudianteModel> result =
                estudianteService.buscarPorSemestre(4);

        // Verificamos que el repositorio fue llamado con semestre 4
        // y que retornó la lista esperada
        assertEquals(estudiantes, result);
    }

    // Buscar por email

    // Escenario: el email existe — retorna el estudiante dentro de un Optional
    @Test
    @DisplayName("Retornar estudiante cuando el email existe")
    void whenSearchByEmail_withExistingEmail_thenReturnEstudiante() {
        var estudiante = buildEstudiante();
        given(estudianteRepository.findByEmail(estudiante.getEmail()))
                .willReturn(Optional.of(estudiante));

        Optional<EstudianteModel> result =
                estudianteService.buscarPorEmail(estudiante.getEmail());

        assertTrue(result.isPresent());
        assertEquals(estudiante, result.get());
    }
}