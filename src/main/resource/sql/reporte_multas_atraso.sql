/* 

tabla a creear antes de ...la ejecucion
CREATE TABLE reporte_multas_atraso (
    anno_mes_pcgc NUMBER(6),
    id_edif       NUMBER(5),
    nombre_edif   VARCHAR2(50),
    total_multas  NUMBER(15)
);

-- =============================================================
-- 1. ALTER TABLE (Para añadir la columna del trigger)
-- =============================================================
-- (Se ejecuta solo si la columna no existe)
BEGIN
   EXECUTE IMMEDIATE 'ALTER TABLE REPORTE_MULTAS_ATRASO ADD (
        fecha_actualizacion DATE
   )';
EXCEPTION
   WHEN OTHERS THEN
      -- Si falla (ej: la columna ya existe), simplemente ignora el error
      IF SQLCODE = -1430 THEN NULL; ELSE RAISE; END IF;
END;
/

-- =============================================================
-- 2. ESPECIFICACIÓN DEL PAQUETE (La "portada")
-- =============================================================
CREATE OR REPLACE PACKAGE pkg_reporte_multas IS

    -- Procedimiento para generar el reporte mensual
    PROCEDURE p_generar_reporte_multas_mes (
        p_anno_mes_pcgc IN NUMBER
    );
    
    -- Función para obtener la multa de un solo edificio
    FUNCTION f_obtener_multa_edificio (
        p_id_edif   IN NUMBER,
        p_anno_mes  IN NUMBER
    )
    RETURN NUMBER;

END pkg_reporte_multas;
/

-- =============================================================
-- 3. CUERPO DEL PAQUETE (El código)
-- =============================================================
CREATE OR REPLACE PACKAGE BODY pkg_reporte_multas IS

    -- --- IMPLEMENTACIÓN DEL PROCEDIMIENTO ---
    PROCEDURE p_generar_reporte_multas_mes (
        p_anno_mes_pcgc IN NUMBER
    )
    IS
        CURSOR cur_multas (c_anno_mes NUMBER) IS
        SELECT
            g.anno_mes_pcgc, e.id_edif, e.nombre_edif,
            SUM(nvl(g.multa_gc, 0)) AS total_multas
        FROM gasto_comun g JOIN edificio e ON g.id_edif = e.id_edif
        WHERE g.anno_mes_pcgc = c_anno_mes
        GROUP BY g.anno_mes_pcgc, e.id_edif, e.nombre_edif;

        v_reg           cur_multas%ROWTYPE; 
        v_mensaje_error VARCHAR2(250);
        v_codigo_error  NUMBER(6);
        v_rutina_error  VARCHAR2(100) := 'PKG_REPORTE_MULTAS.P_GENERAR...';
    BEGIN
        -- Solución para el ORA-12838
        EXECUTE IMMEDIATE 'ALTER SESSION DISABLE PARALLEL DML';

        DELETE FROM REPORTE_MULTAS_ATRASO
        WHERE anno_mes_pcgc = p_anno_mes_pcgc;

        OPEN cur_multas(p_anno_mes_pcgc);
        LOOP
            FETCH cur_multas INTO v_reg;
            EXIT WHEN cur_multas%NOTFOUND;

            INSERT INTO REPORTE_MULTAS_ATRASO
            VALUES (v_reg.anno_mes_pcgc, v_reg.id_edif, v_reg.nombre_edif, v_reg.total_multas, NULL); -- Se inserta NULL en la columna de fecha
        
        END LOOP;
        CLOSE cur_multas;
        COMMIT;

    EXCEPTION
        WHEN OTHERS THEN
            v_mensaje_error := SQLERRM;
            v_codigo_error  := SQLCODE;
            INSERT INTO ERROR_PROCESO
            VALUES (SEQ_ERROR_PROC.NEXTVAL, v_rutina_error,
                    'CODIGO ERROR: ' || v_codigo_error || ' MENSAJE ERROR: ' || v_mensaje_error);
            COMMIT; 
    END p_generar_reporte_multas_mes;

    
    -- --- IMPLEMENTACIÓN DE LA FUNCIÓN ---
    FUNCTION f_obtener_multa_edificio (
        p_id_edif   IN NUMBER,
        p_anno_mes  IN NUMBER
    )
    RETURN NUMBER
    IS
        v_total_multas NUMBER;
    BEGIN
        SELECT SUM(nvl(g.multa_gc, 0))
        INTO v_total_multas
        FROM gasto_comun g
        WHERE g.id_edif = p_id_edif
          AND g.anno_mes_pcgc = p_anno_mes;
          
        RETURN nvl(v_total_multas, 0);
    EXCEPTION
        WHEN OTHERS THEN
            RETURN 0;
    END f_obtener_multa_edificio;

END pkg_reporte_multas;
/

-- =============================================================
-- 4. TRIGGER (La automatización)
-- =============================================================
CREATE OR REPLACE TRIGGER trg_reporte_multas_fecha
BEFORE INSERT OR UPDATE ON REPORTE_MULTAS_ATRASO
FOR EACH ROW
BEGIN
    -- :new se refiere a la fila que se está por insertar o actualizar
    :new.fecha_actualizacion := SYSDATE;
END;


 */

SET SERVEROUTPUT ON;

DECLARE
    v_anno_mes NUMBER := 202504; -- Periodo a procesar (Abril 2025)
    v_total_multas NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('==================================================');
    DBMS_OUTPUT.PUT_LINE('Generando reporte de multas para periodo: ' || v_anno_mes);
    DBMS_OUTPUT.PUT_LINE('==================================================');
    
    pkg_reporte_multas.p_generar_reporte_multas_mes(v_anno_mes);
    
    DBMS_OUTPUT.PUT_LINE('Reporte generado exitosamente.');
    DBMS_OUTPUT.PUT_LINE('==================================================');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
        ROLLBACK;
END;