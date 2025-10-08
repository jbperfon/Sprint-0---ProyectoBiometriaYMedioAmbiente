import uvicorn

if __name__ == "__main__":
    uvicorn.run(
        "backend.api.app:app",  # <-- IMPORTANTE: módulo y objeto correctos
        host="0.0.0.0",
        port=8000,
        reload=True,
    )