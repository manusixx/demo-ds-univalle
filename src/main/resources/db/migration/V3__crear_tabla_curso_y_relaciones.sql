CREATE TABLE curso (
    cu_id          BIGSERIAL    NOT NULL,
    cu_nombre      VARCHAR(150) NOT NULL,
    cu_codigo      VARCHAR(20)  NOT NULL,
    cu_creditos    INTEGER      NOT NULL,
    cu_semestre    VARCHAR(10)  NOT NULL,
    cu_profesor_id BIGINT       NOT NULL,
    CONSTRAINT pk_curso PRIMARY KEY (cu_id),
    CONSTRAINT uq_curso_codigo UNIQUE (cu_codigo),
    CONSTRAINT fk_curso_profesor
        FOREIGN KEY (cu_profesor_id) REFERENCES profesor (pr_id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_curso_creditos CHECK (cu_creditos BETWEEN 1 AND 6)
);

CREATE TABLE curso_estudiante (
    cu_id BIGINT NOT NULL,
    es_id BIGINT NOT NULL,
    CONSTRAINT pk_curso_estudiante PRIMARY KEY (cu_id, es_id),
    CONSTRAINT fk_ce_curso
        FOREIGN KEY (cu_id) REFERENCES curso (cu_id) ON DELETE CASCADE,
    CONSTRAINT fk_ce_estudiante
        FOREIGN KEY (es_id) REFERENCES estudiante (es_id) ON DELETE CASCADE
);
