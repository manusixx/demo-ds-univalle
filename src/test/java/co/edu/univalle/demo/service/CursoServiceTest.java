package co.edu.univalle.demo.service;

import co.edu.univalle.demo.exception.BusinessException;
import co.edu.univalle.demo.exception.ResourceNotFoundException;
import co.edu.univalle.demo.model.CursoModel;
import co.edu.univalle.demo.model.EstudianteModel;
import co.edu.univalle.demo.model.ProfesorModel;
import co.edu.univalle.demo.repository.CursoRepository;
import co.edu.univalle.demo.repository.EstudianteRepository;
import co.edu.univalle.demo.repository.ProfesorRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// Conecta JUnit 5 con Spring para que los @Mock se inicialicen correctamente
@ExtendWith(SpringExtension.class)
class CursoServiceTest {

    // CursoService depende de TRES repositorios — los tres deben ser mocks
    // Esta es la principal diferencia respecto a ProfesorServiceTest
    // y EstudianteServiceTest que solo tenían un repositorio cada uno
    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private ProfesorRepository profesorRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    // El Service REAL que vamos a probar
    private CursoService cursoService;

    // Se ejecuta antes de cada test — inyecta los tres mocks en el service
    @BeforeEach
    void setUp() {
        cursoService = new CursoService(
                cursoRepository, profesorRepository, estudianteRepository);
    }

    // Helpers

    // Construye un ProfesorModel de prueba
    private ProfesorModel buildProfesor() {
        return ProfesorModel.builder()
                .id(1L)
                .nombre("Carlos")
                .apellido("Ramírez")
                .email("carlos@univalle.edu.co")
                .identificacion("12345678")
                .build();
    }

    // Construye un EstudianteModel de prueba
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

    // Construye un CursoModel sin estudiantes — Set vacío por defecto
    private CursoModel buildCurso() {
        return CursoModel.builder()
                .id(1L)
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(buildProfesor())
                .estudiantes(new HashSet<>())
                .build();
    }

    // Crear

    // Escenario feliz: profesor existe, código único, créditos válidos
    @Test
    @DisplayName("Crear curso exitosamente con profesor existente y código único")
    void whenCreateCurso_withValidData_thenReturnSaved() {
        var curso = buildCurso();
        var profesor = buildProfesor();

        // El profesor existe en BD
        given(profesorRepository.findById(1L))
                .willReturn(Optional.of(profesor));

        // El código del curso no existe aún
        given(cursoRepository.findByCodigo(curso.getCodigo()))
                .willReturn(Optional.empty());

        // Al guardar devuelve el curso con id asignado
        given(cursoRepository.save(any(CursoModel.class))).willReturn(curso);

        CursoModel result = cursoService.crear(curso, 1L);

        verify(cursoRepository).save(any(CursoModel.class));
        assertEquals(curso, result);
    }

    // Escenario de error: el profesor no existe — no debe continuar
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al crear con profesor inexistente")
    void whenCreateCurso_withNonExistingProfesor_thenThrowResourceNotFoundException() {
        var curso = buildCurso();

        // El profesor con id 99 no existe
        given(profesorRepository.findById(99L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cursoService.crear(curso, 99L));

        // Si el profesor no existe, save() nunca debe llamarse
        verify(cursoRepository, never()).save(any());
    }

    // Escenario de error: código de curso duplicado
    @Test
    @DisplayName("Lanzar BusinessException al crear con código de curso duplicado")
    void whenCreateCurso_withDuplicateCodigo_thenThrowBusinessException() {
        var curso = buildCurso();

        // El profesor existe
        given(profesorRepository.findById(1L))
                .willReturn(Optional.of(buildProfesor()));

        // Pero el código ya existe en otro curso
        given(cursoRepository.findByCodigo(curso.getCodigo()))
                .willReturn(Optional.of(curso));

        assertThrows(BusinessException.class,
                () -> cursoService.crear(curso, 1L));
        verify(cursoRepository, never()).save(any());
    }

    // Escenario de error: créditos fuera del rango válido (1-6)
    @Test
    @DisplayName("Lanzar BusinessException al crear con créditos inválidos")
    void whenCreateCurso_withInvalidCreditos_thenThrowBusinessException() {
        // Construimos un curso con 7 créditos — fuera del rango permitido
        var curso = CursoModel.builder()
                .id(1L)
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(7)    // ← inválido, máximo es 6
                .semestre("2026-1")
                .profesor(buildProfesor())
                .estudiantes(new HashSet<>())
                .build();

        given(profesorRepository.findById(1L))
                .willReturn(Optional.of(buildProfesor()));
        given(cursoRepository.findByCodigo(curso.getCodigo()))
                .willReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> cursoService.crear(curso, 1L));
        verify(cursoRepository, never()).save(any());
    }

    // Actualizar

    // Escenario feliz: el curso existe y los datos son válidos
    @Test
    @DisplayName("Actualizar curso exitosamente cuando existe")
    void whenUpdateCurso_withExistingId_thenReturnUpdated() {
        var curso = buildCurso();

        given(cursoRepository.findById(curso.getId()))
                .willReturn(Optional.of(curso));
        given(cursoRepository.save(any(CursoModel.class))).willReturn(curso);

        CursoModel result = cursoService.actualizar(curso);

        verify(cursoRepository).save(any(CursoModel.class));
        assertEquals(curso, result);
    }

    // Escenario de error: el curso no existe
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al actualizar curso inexistente")
    void whenUpdateCurso_withNonExistingId_thenThrowResourceNotFoundException() {
        var curso = buildCurso();

        given(cursoRepository.findById(curso.getId()))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cursoService.actualizar(curso));
        verify(cursoRepository, never()).save(any());
    }

    // Escenario de error: créditos inválidos al actualizar
    @Test
    @DisplayName("Lanzar BusinessException al actualizar con créditos inválidos")
    void whenUpdateCurso_withInvalidCreditos_thenThrowBusinessException() {
        var curso = buildCurso();

        given(cursoRepository.findById(curso.getId()))
                .willReturn(Optional.of(curso));

        // Modificamos los créditos a un valor inválido
        curso.setCreditos(0);   // ← por debajo del mínimo

        assertThrows(BusinessException.class,
                () -> cursoService.actualizar(curso));
        verify(cursoRepository, never()).save(any());
    }

    // Eliminar

    // Escenario feliz: el curso existe, se elimina correctamente
    @Test
    @DisplayName("Eliminar curso exitosamente cuando existe")
    void whenDeleteCurso_withExistingId_thenDeleteFromRepository() {
        var curso = buildCurso();

        given(cursoRepository.findById(curso.getId()))
                .willReturn(Optional.of(curso));

        cursoService.eliminar(curso.getId());

        verify(cursoRepository).deleteById(curso.getId());
    }

    // Escenario de error: el curso no existe
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al eliminar curso inexistente")
    void whenDeleteCurso_withNonExistingId_thenThrowResourceNotFoundException() {
        given(cursoRepository.findById(99L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cursoService.eliminar(99L));
        verify(cursoRepository, never()).deleteById(any());
    }

    // Matricular

    // Escenario feliz: curso y estudiante existen, estudiante no matriculado
    // Este es el caso de prueba clave del Paso 10 — verifica que el estudiante
    // queda en el Set del curso después de matricularse
    @Test
    @DisplayName("Matricular estudiante exitosamente en curso")
    void whenMatricularEstudiante_withValidIds_thenEstudianteInSet() {
        var curso = buildCurso();   // Set de estudiantes vacío
        var estudiante = buildEstudiante();

        // Ambos mocks deben configurarse — matricular carga los dos objetos
        given(cursoRepository.findById(1L))
                .willReturn(Optional.of(curso));
        given(estudianteRepository.findById(1L))
                .willReturn(Optional.of(estudiante));
        given(cursoRepository.save(curso)).willReturn(curso);

        CursoModel result = cursoService.matricularEstudiante(1L, 1L);

        // Verificamos que el estudiante quedó en el Set del curso
        assertTrue(result.getEstudiantes().contains(estudiante));
        verify(cursoRepository).save(curso);
    }

    // Escenario de error: el curso no existe al matricular
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al matricular en curso inexistente")
    void whenMatricularEstudiante_withNonExistingCurso_thenThrowResourceNotFoundException() {
        given(cursoRepository.findById(99L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cursoService.matricularEstudiante(99L, 1L));
        verify(cursoRepository, never()).save(any());
    }

    // Escenario de error: el estudiante no existe al matricular
    @Test
    @DisplayName("Lanzar ResourceNotFoundException al matricular estudiante inexistente")
    void whenMatricularEstudiante_withNonExistingEstudiante_thenThrowResourceNotFoundException() {
        var curso = buildCurso();

        given(cursoRepository.findById(1L))
                .willReturn(Optional.of(curso));

        // El estudiante con id 99 no existe
        given(estudianteRepository.findById(99L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cursoService.matricularEstudiante(1L, 99L));
        verify(cursoRepository, never()).save(any());
    }

    // Escenario de error: estudiante ya matriculado — caso clave del Paso 10
    // construimos el curso con el estudiante ya en el Set
    // para simular que ya estaba matriculado previamente
    @Test
    @DisplayName("Lanzar BusinessException al matricular estudiante ya matriculado")
    void whenMatricularEstudiante_alreadyEnrolled_thenThrowBusinessException() {
        var estudiante = buildEstudiante();

        // Construimos el curso CON el estudiante ya en el Set
        var curso = CursoModel.builder()
                .id(1L)
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(buildProfesor())
                .estudiantes(new HashSet<>(Set.of(estudiante)))  // ya matriculado
                .build();

        given(cursoRepository.findById(1L))
                .willReturn(Optional.of(curso));
        given(estudianteRepository.findById(1L))
                .willReturn(Optional.of(estudiante));

        // El service debe detectar que ya está en el Set y lanzar BusinessException
        assertThrows(BusinessException.class,
                () -> cursoService.matricularEstudiante(1L, 1L));
        verify(cursoRepository, never()).save(any());
    }

    // Retirar

    // Escenario feliz: estudiante matriculado, se retira correctamente
    // caso clave del Paso 10 — el Set debe quedar vacío después de retirar
    @Test
    @DisplayName("Retirar estudiante exitosamente del curso")
    void whenRetirarEstudiante_withEnrolledEstudiante_thenSetEmpty() {
        var estudiante = buildEstudiante();

        // Construimos el curso CON el estudiante en el Set
        var curso = CursoModel.builder()
                .id(1L)
                .nombre("Desarrollo de Software III")
                .codigo("DS-101")
                .creditos(3)
                .semestre("2026-1")
                .profesor(buildProfesor())
                .estudiantes(new HashSet<>(Set.of(estudiante)))  // matriculado
                .build();

        given(cursoRepository.findById(1L))
                .willReturn(Optional.of(curso));
        given(estudianteRepository.findById(1L))
                .willReturn(Optional.of(estudiante));
        given(cursoRepository.save(curso)).willReturn(curso);

        CursoModel result = cursoService.retirarEstudiante(1L, 1L);

        // Verificamos que el Set quedó vacío después de retirar
        assertTrue(result.getEstudiantes().isEmpty());
        verify(cursoRepository).save(curso);
    }

    // Escenario de error: estudiante NO matriculado — caso clave del Paso 10
    // el curso tiene el Set vacío — no hay nadie que retirar
    @Test
    @DisplayName("Lanzar BusinessException al retirar estudiante no matriculado")
    void whenRetirarEstudiante_notEnrolled_thenThrowBusinessException() {
        var curso = buildCurso();   // Set vacío — nadie matriculado
        var estudiante = buildEstudiante();

        given(cursoRepository.findById(1L))
                .willReturn(Optional.of(curso));
        given(estudianteRepository.findById(1L))
                .willReturn(Optional.of(estudiante));

        // El service detecta que el estudiante NO está en el Set
        assertThrows(BusinessException.class,
                () -> cursoService.retirarEstudiante(1L, 1L));
        verify(cursoRepository, never()).save(any());
    }

    // Obtener todos

    @Test
    @DisplayName("Retornar lista de todos los cursos")
    void whenGetAllCursos_thenReturnList() {
        var cursos = List.of(buildCurso());
        given(cursoRepository.findAll()).willReturn(cursos);

        List<CursoModel> result = cursoService.obtenerTodos();

        verify(cursoRepository).findAll();
        assertEquals(cursos, result);
    }

    // Obtener por id

    @Test
    @DisplayName("Retornar curso por id cuando existe")
    void whenGetCursoById_withExistingId_thenReturnCurso() {
        var curso = buildCurso();
        given(cursoRepository.findById(1L))
                .willReturn(Optional.of(curso));

        CursoModel result = cursoService.obtenerPorId(1L);

        assertEquals(curso, result);
    }

    @Test
    @DisplayName("Lanzar ResourceNotFoundException al buscar id inexistente")
    void whenGetCursoById_withNonExistingId_thenThrowResourceNotFoundException() {
        given(cursoRepository.findById(99L))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cursoService.obtenerPorId(99L));
    }

    // Buscar por semestre

    @Test
    @DisplayName("Retornar cursos del semestre indicado")
    void whenSearchBySemestre_thenReturnMatchingCursos() {
        var cursos = List.of(buildCurso());
        given(cursoRepository.findAllBySemestre("2026-1"))
                .willReturn(cursos);

        List<CursoModel> result = cursoService.buscarPorSemestre("2026-1");

        assertEquals(cursos, result);
    }

    // Buscar por profesor

    @Test
    @DisplayName("Retornar cursos del profesor indicado")
    void whenSearchByProfesor_thenReturnMatchingCursos() {
        var cursos = List.of(buildCurso());
        given(cursoRepository.findAllByProfesorId(1L))
                .willReturn(cursos);

        List<CursoModel> result = cursoService.buscarPorProfesor(1L);

        assertEquals(cursos, result);
    }

    // Buscar por estudiante

    @Test
    @DisplayName("Retornar cursos del estudiante indicado")
    void whenSearchByEstudiante_thenReturnMatchingCursos() {
        var cursos = List.of(buildCurso());
        given(cursoRepository.findCursosByEstudianteId(1L))
                .willReturn(cursos);

        List<CursoModel> result = cursoService.buscarPorEstudiante(1L);

        assertEquals(cursos, result);
    }
}