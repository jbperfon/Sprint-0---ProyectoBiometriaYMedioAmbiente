import sqlite3
import pytest
from fastapi.testclient import TestClient
from ..app import app                 # relativo al paquete api
from ..routes import get_logica       # relativo al paquete api
from ...logica.logica import Logica   # subir a backend/logica


client = TestClient(app)


def override_get_logica(db_path):
    """Crea una instancia de Logica apuntando a una BD de prueba."""
    def _get_logica():
        return Logica(ruta_bd=db_path)
    return _get_logica


@pytest.fixture
def db_temporal(tmp_path):
    """BD temporal para cada test de la API."""
    db_file = tmp_path / "api_test.db"
    Logica(ruta_bd=db_file)  # Crea estructura
    return str(db_file)


def test_post_medicion_crea_registro(db_temporal):
    """POST /api/v1/mediciones debería insertar una medición."""
    app.dependency_overrides[get_logica] = override_get_logica(db_temporal)

    response = client.post(
        "/api/v1/mediciones",
        json={"medida": 999, "rssi": -60, "timestamp": "2025-10-06T12:30:00"}
    )

    assert response.status_code == 201
    data = response.json()
    assert "id" in data

    # Verificamos que realmente se guardó
    with sqlite3.connect(db_temporal) as conn:
        cur = conn.execute("SELECT medida FROM mediciones")
        row = cur.fetchone()
    assert row is not None
    assert row[0] == 999


def test_get_ultima_medicion_ok(db_temporal):
    """GET /api/v1/mediciones/ultima debe devolver la última."""
    app.dependency_overrides[get_logica] = override_get_logica(db_temporal)
    logica = Logica(ruta_bd=db_temporal)
    logica.guardar_medicion(333, rssi=-70, timestamp="2025-10-06T10:00:00")

    response = client.get("/api/v1/mediciones/ultima")
    assert response.status_code == 200
    data = response.json()
    assert data["medida"] == 333
    assert "timestamp" in data


def test_get_ultima_medicion_sin_datos(tmp_path):
    """Debe devolver 404 si la tabla está vacía."""
    db_file = tmp_path / "empty.db"
    Logica(ruta_bd=db_file)
    app.dependency_overrides[get_logica] = override_get_logica(str(db_file))

    response = client.get("/api/v1/mediciones/ultima")
    assert response.status_code == 404
    assert "No hay mediciones" in response.json()["detail"]