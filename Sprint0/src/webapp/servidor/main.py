from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# -------------------------------------------------------------------
# Crear instancia de la aplicación
# -------------------------------------------------------------------
app = FastAPI(
    title="API Proyecto Biometría y Medioambiente",
    version="1.0.0",
    description="Backend para recibir y gestionar mediciones enviadas por la app Android"
)

# -------------------------------------------------------------------
# Configurar CORS (para permitir acceso desde el frontend)
# -------------------------------------------------------------------
origins = [
    "http://localhost",
    "http://localhost:5500",   # si sirves el frontend con Live Server de VSCode
    "http://127.0.0.1:5500"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# -------------------------------------------------------------------
# Rutas básicas (más adelante se importarán desde /routes)
# -------------------------------------------------------------------
@app.get("/health")
def health_check():
    """Verifica que la API está en funcionamiento."""
    return {"status": "ok", "message": "API funcionando correctamente"}