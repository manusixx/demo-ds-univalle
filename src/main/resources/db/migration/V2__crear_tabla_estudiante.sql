CREATE TABLE estudiante (
    es_id             BIGSERIAL    NOT NULL,
    es_nombre         VARCHAR(100) NOT NULL,
    es_apellido       VARCHAR(100) NOT NULL,
    es_email          VARCHAR(150) NOT NULL,
    es_identificacion VARCHAR(20)  NOT NULL,
    es_semestre       INTEGER      NOT NULL,
    CONSTRAINT pk_estudiante PRIMARY KEY (es_id),
    CONSTRAINT uq_estudiante_email UNIQUE (es_email),
    CONSTRAINT uq_estudiante_identificacion UNIQUE (es_identificacion),
    CONSTRAINT ck_estudiante_semestre CHECK (es_semestre BETWEEN 1 AND 10)
);
