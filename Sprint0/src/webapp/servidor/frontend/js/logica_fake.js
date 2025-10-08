/**
 * Capa "lógica" del frontend: funciones de alto nivel para hablar con tu API.
 * Endpoints esperados:
 *   POST /api/v1/mediciones   body: {medida:int, rssi?:int, timestamp:string}
 *   GET  /api/v1/mediciones/ultima
 *   GET  /health
 */
class LogicaFake {
  constructor(baseUrl) {
    this.baseUrl = (baseUrl || '').replace(/\/$/, '');
    this.http = new PeticionarioREST({ baseUrl: this.baseUrl });
  }

  setBaseUrl(baseUrl) {
    this.baseUrl = (baseUrl || '').replace(/\/$/, '');
    this.http = new PeticionarioREST({ baseUrl: this.baseUrl });
  }

  ping() {
    return this.http.get('/health');
  }

  obtenerUltima() {
    return this.http.get('/api/v1/mediciones/ultima');
  }

  enviarMedicion({ medida, rssi, timestamp }) {
    const body = { medida: Number(medida), timestamp: String(timestamp) };
    if (rssi !== undefined && rssi !== null && String(rssi) !== '') {
      body.rssi = Number(rssi);
    }
    return this.http.post('/api/v1/mediciones', { body });
  }
}
window.LogicaFake = LogicaFake;