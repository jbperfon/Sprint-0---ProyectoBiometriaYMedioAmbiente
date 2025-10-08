# backend/api/app.py
from fastapi import FastAPI
from .routes import router as api_router
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
from pathlib import Path

app = FastAPI(title="Beacon API", version="1.0.0")
app.include_router(api_router, prefix="/api/v1")

BASE_DIR = Path(__file__).resolve().parent.parent.parent
FRONTEND_DIR = BASE_DIR / "frontend"

# estáticos (sirven /js y /css)
app.mount("/js", StaticFiles(directory=FRONTEND_DIR / "js"), name="js")
app.mount("/css", StaticFiles(directory=FRONTEND_DIR / "css"), name="css")

# ÚNICA ruta raíz -> index.html
@app.get("/", include_in_schema=False)
async def root():
    return FileResponse(FRONTEND_DIR / "index.html")
