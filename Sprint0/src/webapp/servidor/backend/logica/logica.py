# backend/logica/logica.py
from __future__ import annotations
import sqlite3
from dataclasses import dataclass
from pathlib import Path
from typing import Optional, Tuple

SQL_CREATE = """
CREATE TABLE IF NOT EXISTS mediciones (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    medicion   INTEGER NOT NULL,
    timestamp  TEXT NOT NULL
);
"""

SQL_INSERT = """
INSERT INTO mediciones (medicion, timestamp)
VALUES (?, COALESCE(?, strftime('%Y-%m-%dT%H:%M:%S','now')))
;
"""

SQL_SELECT_ULTIMA = """
SELECT medicion, timestamp
FROM mediciones
ORDER BY id DESC
LIMIT 1;
"""

@dataclass
class Logica:
    ruta_bd: str | Path

    def __post_init__(self) -> None:
        self._db_path = self._normalizar_ruta(self.ruta_bd)
        self._asegurar_esquema()

    def guardar_medicion(self, medicion: int, *, timestamp: str) -> int:
        self._validar_medicion(medicion)
        self._validar_timestamp(timestamp)
        try:
            with self._connect() as conn:
                cur = conn.execute(SQL_INSERT, (int(medicion), timestamp))
                conn.commit()
                return int(cur.lastrowid)
        except Exception as e:
            raise RuntimeError(f"Error guardando la medición: {e}") from e

    def obtener_ultima_medida(self) -> Optional[Tuple[int, str]]:
        try:
            with self._connect() as conn:
                cur = conn.execute(SQL_SELECT_ULTIMA)
                row = cur.fetchone()
                if row is None:
                    return None
                medicion, ts = row
                return int(medicion), str(ts)
        except Exception as e:
            raise RuntimeError(f"Error obteniendo la última medición: {e}") from e

    @staticmethod
    def _normalizar_ruta(ruta: str | Path) -> Path:
        p = Path(ruta)
        if p.suffix == "":
            p = p / "mediciones.db"
        p.parent.mkdir(parents=True, exist_ok=True)
        return p

    def _connect(self) -> sqlite3.Connection:
        return sqlite3.connect(self._db_path, check_same_thread=False)

    def _asegurar_esquema(self) -> None:
        try:
            with self._connect() as conn:
                conn.execute(SQL_CREATE)
                conn.commit()
        except Exception as e:
            raise RuntimeError(f"Error inicializando el esquema: {e}") from e

    @staticmethod
    def _validar_medicion(medicion: int) -> None:
        if medicion is None:
            raise ValueError("La medición no puede ser None.")
        if not isinstance(medicion, int):
            raise ValueError("La medición debe ser un entero.")

    @staticmethod
    def _validar_timestamp(ts: str) -> None:
        if not ts or not isinstance(ts, str):
            raise ValueError("El timestamp es obligatorio y debe ser texto.")