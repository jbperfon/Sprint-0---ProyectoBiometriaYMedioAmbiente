from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field, validator
from ..logica.logica import Logica
from pathlib import Path
from datetime import datetime
from typing import Optional

router = APIRouter()


# ---------- Configuración de acceso a la base de datos ----------
def get_logica():
    base = Path(__file__).resolve().parent.parent
    db_path = base / "db" / "mediciones.db"
    return Logica(ruta_bd=str(db_path))


# ---------- Modelo de entrada ----------
class MedicionIn(BaseModel):
    medicion: int = Field(..., example=1234)
    timestamp: str = Field(
        ...,
        example="2025-10-08T12:00:00Z",
        description="Fecha y hora en formato ISO 8601, por ejemplo: 2025-10-08T12:00:00Z"
    )

    # Validador: asegura que el timestamp tiene formato ISO 8601
    @validator("timestamp")
    def validar_timestamp_iso(cls, v):
        try:
            # Permitir formato con Z o con zona horaria
            datetime.fromisoformat(v.replace("Z", "+00:00"))
            return v
        except Exception:
            raise ValueError(
                "El timestamp debe tener formato ISO 8601, p. ej. 2025-10-08T12:00:00Z"
            )


# ---------- Endpoint: insertar nueva medición ----------
@router.post("/mediciones", status_code=201)
def crear_medicion(payload: MedicionIn, logica: Logica = Depends(get_logica)):
    try:
        nuevo_id = logica.guardar_medicion(
            medicion=payload.medicion,
            timestamp=payload.timestamp,
        )

        return {
            "id": nuevo_id,
            "message": "Medición guardada correctamente",
            "timestamp": payload.timestamp,
        }

    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))


# ---------- Endpoint: obtener la última medición ----------
@router.get("/mediciones/ultima")
def ultima(logica: Logica = Depends(get_logica)):
    try:
        fila = logica.obtener_ultima_medida()
        if not fila:
            raise HTTPException(status_code=404, detail="No hay mediciones aún")
        medicion, ts = fila
        return {"medicion": medicion, "timestamp": ts}
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))