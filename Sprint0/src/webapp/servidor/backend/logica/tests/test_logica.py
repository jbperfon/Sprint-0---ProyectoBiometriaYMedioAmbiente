import sqlite3
import pytest
from ..logica import Logica    # relativo al paquete logica


@pytest.fixture
def db_temporal(tmp_path):
    """Crea una base de datos SQLite temporal para cada test."""
    db_file = tmp_path / "test_mediciones.db"
    logica = Logica(ruta_bd=db_file)
    return str(db_file)


def test_guardar_medicion_inserta_en_bd(db_temporal):
    """Verifica que guardar_medicion inserta una fila en la tabla."""
    logica = Logica(ruta_bd=db_temporal)
    logica.guardar_medicion(medida=123, rssi=-70, timestamp="2025-10-06T12:00:00")

    with sqlite3.connect(db_temporal) as conn:
        cursor = conn.execute("SELECT medida, rssi, timestamp FROM mediciones")
        fila = cursor.fetchone()

    assert fila is not None
    assert fila[0] == 123
    assert fila[1] == -70
    assert fila[2] == "2025-10-06T12:00:00"


def test_obtener_ultima_medida_devuelve_correcto(db_temporal):
    """Comprueba que obtener_ultima_medida devuelve la última inserción."""
    logica = Logica(ruta_bd=db_temporal)
    logica.guardar_medicion(100, rssi=-70, timestamp="2025-10-06T12:00:00")
    logica.guardar_medicion(200, rssi=-50, timestamp="2025-10-06T12:10:00")

    resultado = logica.obtener_ultima_medida()

    assert resultado is not None
    medida, rssi, ts = resultado
    assert medida == 200
    assert rssi == -50
    assert ts == "2025-10-06T12:10:00"


def test_obtener_ultima_medida_vacia(tmp_path):
    """Debe devolver None si no hay registros en la tabla."""
    db_file = tmp_path / "empty.db"
    logica = Logica(ruta_bd=db_file)
    resultado = logica.obtener_ultima_medida()
    assert resultado is None