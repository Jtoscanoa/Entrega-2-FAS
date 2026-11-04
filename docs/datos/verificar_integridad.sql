-- Verificación de integridad del esquema (E2 §2.2). Ejecutar sobre una base vacía
-- después de schema.sql:  psql -v ON_ERROR_STOP=0 -f verificar_integridad.sql
-- Cada bloque DO falla a propósito y muestra el NOTICE "OK: ..." si la base lo rechaza.
INSERT INTO jugador (sub_oidc, alias) VALUES ('oidc|ana', 'ana'), ('oidc|beto', 'beto');
INSERT INTO sala (codigo, anfitrion_id) VALUES ('K7Q2ZP', 1);
INSERT INTO partida (sala_codigo, estado) VALUES ('K7Q2ZP', 'EN_CURSO');
INSERT INTO configuracion_reglas (partida_id, modalidad) VALUES (1, 'APUNTADO_101');
INSERT INTO participante (partida_id, jugador_id, asiento) VALUES (1, 1, 1), (1, 2, 2);
INSERT INTO ronda (partida_id, numero, abridor_participante_id) VALUES (1, 1, 1);

DO $$ BEGIN
  UPDATE configuracion_reglas SET residuo_maximo = 5 WHERE partida_id = 1;
  RAISE EXCEPTION 'FALLO: aceptó residuo máximo 5';
EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: rechaza residuo máximo de 5';
END $$;

DO $$ BEGIN
  INSERT INTO participante (partida_id, asiento) VALUES (1, 3);
  RAISE EXCEPTION 'FALLO: aceptó participante sin jugador ni alias';
EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: rechaza participante sin jugador ni alias de bot';
END $$;

DO $$ BEGIN
  UPDATE participante SET es_ganador = TRUE WHERE partida_id = 1;
  RAISE EXCEPTION 'FALLO: aceptó dos ganadores';
EXCEPTION WHEN unique_violation THEN RAISE NOTICE 'OK: rechaza dos ganadores en la misma partida';
END $$;

DO $$ BEGIN
  INSERT INTO ronda (partida_id, numero, abridor_participante_id) VALUES (1, 1, 2);
  RAISE EXCEPTION 'FALLO: aceptó dos rondas con el mismo número';
EXCEPTION WHEN unique_violation THEN RAISE NOTICE 'OK: rechaza dos rondas con el mismo número';
END $$;

-- Liquidación idempotente (§2.4, barrera 3): la segunda no afecta filas.
DO $$ DECLARE n INT; BEGIN
  UPDATE ronda SET estado = 'LIQUIDADA', cerrador_participante_id = 1, tipo_cierre = 'APUNTADO', liquidada_en = now()
   WHERE id = 1 AND estado = 'EN_CURSO';
  GET DIAGNOSTICS n = ROW_COUNT;
  IF n <> 1 THEN RAISE EXCEPTION 'FALLO: la primera liquidación afectó % filas', n; END IF;
  UPDATE ronda SET estado = 'LIQUIDADA', cerrador_participante_id = 2, tipo_cierre = 'COMPLETO', liquidada_en = now()
   WHERE id = 1 AND estado = 'EN_CURSO';
  GET DIAGNOSTICS n = ROW_COUNT;
  IF n <> 0 THEN RAISE EXCEPTION 'FALLO: la segunda liquidación afectó % filas', n; END IF;
  RAISE NOTICE 'OK: una segunda liquidación de la misma ronda no afecta filas';
END $$;
