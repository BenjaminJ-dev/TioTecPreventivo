CREATE TABLE IF NOT EXISTS acciones_preventivas (
    id INTEGER PRIMARY KEY,
    completada BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS preguntas (
    id INTEGER PRIMARY KEY,
    texto VARCHAR(512) NOT NULL,
    orden INTEGER NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS respuestas_evaluacion (
    perfil_id UUID NOT NULL,
    pregunta_id INTEGER NOT NULL REFERENCES preguntas (id),
    respuesta BOOLEAN NOT NULL,
    PRIMARY KEY (perfil_id, pregunta_id)
);
