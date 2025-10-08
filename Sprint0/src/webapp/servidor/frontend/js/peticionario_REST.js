/**
 * Pequeño cliente REST basado en fetch, con timeout y JSON automático.
 * No depende de frameworks.
 */
class PeticionarioREST {
  constructor({ baseUrl, defaultHeaders } = {}) {
    this.baseUrl = (baseUrl || '').replace(/\/$/, '');
    this.defaultHeaders = Object.assign({ 'Content-Type': 'application/json' }, defaultHeaders || {});
  }

  async request(method, path, { body, headers, timeoutMs = 8000 } = {}) {
    const controller = new AbortController();
    const t = setTimeout(() => controller.abort(), timeoutMs);

    const url = `${this.baseUrl}${path}`;
    const opts = {
      method,
      headers: Object.assign({}, this.defaultHeaders, headers || {}),
      signal: controller.signal
    };
    if (body !== undefined) {
      opts.body = typeof body === 'string' ? body : JSON.stringify(body);
    }

    try {
      const res = await fetch(url, opts);
      const text = await res.text(); // puede no ser JSON
      let json;
      try { json = text ? JSON.parse(text) : null; } catch { json = text; }

      if (!res.ok) {
        const err = new Error(`HTTP ${res.status}`);
        err.status = res.status;
        err.payload = json;
        throw err;
      }
      return json;
    } finally {
      clearTimeout(t);
    }
  }

  get(path, opts)    { return this.request('GET', path, opts); }
  post(path, opts)   { return this.request('POST', path, opts); }
  put(path, opts)    { return this.request('PUT', path, opts); }
  delete(path, opts) { return this.request('DELETE', path, opts); }
}
window.PeticionarioREST = PeticionarioREST;