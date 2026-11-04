-- Esquema relacional del Apuntado a 101 (E2 §2.2, Figura 3). PostgreSQL 16, 3FN.
-- 13 tablas. Solo MatchStateRepository accede a la base (ADR-05).

CREATE TABLE jugador (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sub_oidc      VARCHAR(255) NOT NULL UNIQUE,            -- clave natural (ADR-07)
    alias         VARCHAR(40)  NOT NULL,
    saldo_fichas  INTEGER      NOT NULL DEFAULT 1000 CHECK (saldo_fichas >= 0),
    creado_en     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE sala (
    codigo        CHAR(6)     PRIMARY KEY,
    anfitrion_id  BIGINT      NOT NULL REFERENCES jugador(id),
    creada_en     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE partida (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sala_codigo   CHAR(6)     NOT NULL REFERENCES sala(codigo),
    estado        VARCHAR(20) NOT NULL CHECK (estado IN ('CONFIGURADA', 'EN_CURSO', 'FINALIZADA')),
    pozo          INTEGER     NOT NULL DEFAULT 0 CHECK (pozo >= 0),
    version       INTEGER     NOT NULL DEFAULT 0,          -- bloqueo optimista
    iniciada_en   TIMESTAMPTZ,
    finalizada_en TIMESTAMPTZ
);

CREATE TABLE configuracion_reglas (
    partida_id            BIGINT PRIMARY KEY REFERENCES partida(id) ON DELETE CASCADE,
    modalidad             VARCHAR(30) NOT NULL,
    umbral_eliminacion    SMALLINT NOT NULL DEFAULT 101 CHECK (umbral_eliminacion > 0),
    residuo_maximo        SMALLINT NOT NULL DEFAULT 4 CHECK (residuo_maximo BETWEEN 0 AND 4),
    comodines_habilitados BOOLEAN  NOT NULL DEFAULT FALSE,
    robar_del_descarte    BOOLEAN  NOT NULL DEFAULT FALSE,
    reingreso_permitido   BOOLEAN  NOT NULL DEFAULT FALSE,
    cuota_reingreso       INTEGER  NOT NULL DEFAULT 0 CHECK (cuota_reingreso >= 0),
    max_reingresos        SMALLINT NOT NULL DEFAULT 0 CHECK (max_reingresos >= 0),
    apuesta               INTEGER  NOT NULL DEFAULT 0 CHECK (apuesta >= 0),
    meta_victoria         SMALLINT NOT NULL DEFAULT -51,
    max_rondas            SMALLINT NOT NULL DEFAULT 99 CHECK (max_rondas >= 1),
    cierre_a_pintas       BOOLEAN  NOT NULL DEFAULT FALSE,
    segundo_mazo          BOOLEAN  NOT NULL DEFAULT FALSE,
    segundos_por_turno    SMALLINT NOT NULL DEFAULT 30 CHECK (segundos_por_turno >= 5)
);

CREATE TABLE movimiento_fichas (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jugador_id    BIGINT      NOT NULL REFERENCES jugador(id),
    partida_id    BIGINT      REFERENCES partida(id),      -- NULL: movimiento fuera de partida
    tipo          VARCHAR(16) NOT NULL CHECK (tipo IN ('APUESTA', 'POZO', 'REINGRESO', 'RECARGA')),
    monto         INTEGER     NOT NULL,
    registrado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE participante (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partida_id  BIGINT      NOT NULL REFERENCES partida(id) ON DELETE CASCADE,
    jugador_id  BIGINT      REFERENCES jugador(id),        -- NULL = bot
    alias_bot   VARCHAR(20),
    asiento     SMALLINT    NOT NULL CHECK (asiento BETWEEN 1 AND 6),
    estado      VARCHAR(10) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'ELIMINADO', 'RETIRADO')),
    reingresos  SMALLINT    NOT NULL DEFAULT 0 CHECK (reingresos >= 0),
    es_ganador  BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT participante_humano_o_bot CHECK (jugador_id IS NOT NULL OR alias_bot IS NOT NULL),
    CONSTRAINT participante_asiento_unico UNIQUE (partida_id, asiento)
);
-- A lo sumo un ganador por partida
CREATE UNIQUE INDEX participante_un_ganador ON participante (partida_id) WHERE es_ganador;

CREATE TABLE ronda (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partida_id               BIGINT      NOT NULL REFERENCES partida(id) ON DELETE CASCADE,
    numero                   SMALLINT    NOT NULL CHECK (numero >= 1),
    abridor_participante_id  BIGINT      NOT NULL REFERENCES participante(id),
    cerrador_participante_id BIGINT      REFERENCES participante(id),
    tipo_cierre              VARCHAR(15) CHECK (tipo_cierre IN ('CLASICO_433', 'A_PINTAS', 'COMPLETO', 'APUNTADO')),
    estado                   VARCHAR(10) NOT NULL DEFAULT 'EN_CURSO' CHECK (estado IN ('EN_CURSO', 'LIQUIDADA', 'ANULADA')),
    iniciada_en              TIMESTAMPTZ NOT NULL DEFAULT now(),
    liquidada_en             TIMESTAMPTZ,
    CONSTRAINT ronda_numero_unico UNIQUE (partida_id, numero),       -- 1:N partida -> rondas
    CONSTRAINT ronda_liquidada_completa CHECK (estado <> 'LIQUIDADA'
        OR (cerrador_participante_id IS NOT NULL AND tipo_cierre IS NOT NULL AND liquidada_en IS NOT NULL))
);

CREATE TABLE sello_reparto (
    ronda_id       BIGINT      NOT NULL REFERENCES ronda(id) ON DELETE CASCADE,
    secuencia      SMALLINT    NOT NULL CHECK (secuencia >= 1),      -- 1 = reparto; 2.. = reposiciones (RF14)
    huella_sha256  CHAR(64)    NOT NULL,
    sal            CHAR(32),
    orden_revelado TEXT,                                             -- cadena exacta que se hashea (ADR-03)
    publicado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    revelado_en    TIMESTAMPTZ,
    PRIMARY KEY (ronda_id, secuencia),
    CONSTRAINT sello_revelado_completo CHECK ((sal IS NULL) = (orden_revelado IS NULL))
);

CREATE TABLE puntaje_acumulado (
    ronda_id        BIGINT      NOT NULL REFERENCES ronda(id) ON DELETE CASCADE,
    participante_id BIGINT      NOT NULL REFERENCES participante(id),
    evento          VARCHAR(10) NOT NULL CHECK (evento IN ('RONDA', 'REINGRESO')),
    total           SMALLINT    NOT NULL,
    PRIMARY KEY (ronda_id, participante_id, evento)
);

CREATE TABLE mano_jugador (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ronda_id        BIGINT   NOT NULL REFERENCES ronda(id) ON DELETE CASCADE,
    participante_id BIGINT   NOT NULL REFERENCES participante(id),
    puntos_ronda    SMALLINT NOT NULL,
    es_quien_cierra BOOLEAN  NOT NULL DEFAULT FALSE,
    CONSTRAINT mano_unica_por_ronda UNIQUE (ronda_id, participante_id)
);

CREATE TABLE carta (
    codigo     VARCHAR(3) PRIMARY KEY,                    -- "7C", "10T", "*"
    rango      SMALLINT   NOT NULL CHECK (rango BETWEEN 0 AND 13),
    pinta      CHAR(1)    CHECK (pinta IN ('C', 'D', 'T', 'P')),
    es_comodin BOOLEAN    NOT NULL DEFAULT FALSE,
    CONSTRAINT carta_comodin_sin_pinta CHECK (es_comodin = (pinta IS NULL))
);

CREATE TABLE carta_mano (
    mano_id      BIGINT     NOT NULL REFERENCES mano_jugador(id) ON DELETE CASCADE,
    posicion     SMALLINT   NOT NULL CHECK (posicion BETWEEN 1 AND 11),
    carta_codigo VARCHAR(3) NOT NULL REFERENCES carta(codigo),
    combinacion  SMALLINT,                                 -- NULL = carta suelta
    PRIMARY KEY (mano_id, posicion)
);

CREATE TABLE turno_jugada (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ronda_id        BIGINT      NOT NULL REFERENCES ronda(id) ON DELETE CASCADE,
    numero_turno    INTEGER     NOT NULL CHECK (numero_turno >= 0),
    secuencia       SMALLINT    NOT NULL CHECK (secuencia >= 1),
    participante_id BIGINT      NOT NULL REFERENCES participante(id),
    accion          VARCHAR(20) NOT NULL CHECK (accion IN ('ROBAR_MAZO', 'ROBAR_DESCARTE', 'DESCARTAR', 'CERRAR', 'VENCIMIENTO')),
    carta_codigo    VARCHAR(3)  REFERENCES carta(codigo),
    ocurrida_en     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT turno_secuencia_unica UNIQUE (ronda_id, numero_turno, secuencia)
);

-- Catálogo de cartas: 52 naturales + comodín
INSERT INTO carta (codigo, rango, pinta, es_comodin)
SELECT r.c || p.p, r.n, p.p, FALSE
FROM (VALUES ('A',1),('2',2),('3',3),('4',4),('5',5),('6',6),('7',7),('8',8),('9',9),('10',10),('J',11),('Q',12),('K',13)) AS r(c, n)
CROSS JOIN (VALUES ('C'),('D'),('T'),('P')) AS p(p);
INSERT INTO carta (codigo, rango, pinta, es_comodin) VALUES ('*', 0, NULL, TRUE);
