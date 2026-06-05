CREATE TABLE profesor (
    pr_id             BIGSERIAL       NOT NULL,
    pr_nombre         VARCHAR(100)    NOT NULL,
    pr_apellido       VARCHAR(100)    NOT NULL,
    pr_email          VARCHAR(150)    NOT NULL,
    pr_identificacion VARCHAR(20)     NOT NULL,
    CONSTRAINT pk_profesor PRIMARY KEY (pr_id),
    CONSTRAINT uq_profesor_email UNIQUE (pr_email),
    CONSTRAINT uq_profesor_identificacion UNIQUE (pr_identificacion)
);
