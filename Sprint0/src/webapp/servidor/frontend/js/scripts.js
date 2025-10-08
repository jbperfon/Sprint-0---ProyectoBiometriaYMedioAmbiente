// Configura aquí la URL base de tu API (FastAPI en 8000)

const API_BASE = "http://127.0.0.1:8000/api/v1";

const tablaBody = document.querySelector("#tabla-mediciones tbody");
const btnCargar = document.getElementById("btn-cargar");
const btnVaciar = document.getElementById("btn-vaciar");
const chkAuto = document.getElementById("chk-auto");
const msg = document.getElementById("msg");

let autoTimer = null;

function showMsg(text, isError = false) {
  msg.textContent = text;
  msg.classList.toggle("error", isError);
  msg.classList.remove("hidden");
  clearTimeout(showMsg._t);
  showMsg._t = setTimeout(() => msg.classList.add("hidden"), 3000);
}

function addRow({ id = "", medida = "", medicion = "", timestamp = "" }) {
  // algunos endpoints podrían devolver 'medicion' en lugar de 'medida'
  const value = medida !== "" ? medida : medicion;

  const tr = document.createElement("tr");
  tr.innerHTML = `
    <td>${id === undefined || id === null ? "" : id}</td>
    <td>${value ?? ""}</td>
    <td>${timestamp ?? ""}</td>
  `;
  tablaBody.appendChild(tr);
}

async function fetchUltima() {
  const url = `${API_BASE}/mediciones/ultima`;
  const res = await fetch(url, { headers: { "Accept": "application/json" } });
  if (!res.ok) {
    const t = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status} - ${t || res.statusText}`);
  }
  return res.json();
}

async function handleCargar() {
  try {
    const data = await fetchUltima();
    // si el backend devuelve un objeto simple, lo envolvemos como único
    const items = Array.isArray(data) ? data : [data];
    items.forEach(addRow);
    showMsg("Última medición añadida.");
  } catch (err) {
    console.error(err);
    showMsg("No se pudo obtener la última medición.", true);
  }
}

function handleVaciar() {
  tablaBody.innerHTML = "";
  showMsg("Tabla vaciada.");
}

function handleAutoToggle() {
  if (chkAuto.checked) {
    autoTimer = setInterval(handleCargar, 5000);
    showMsg("Auto-actualización activada (5s).");
  } else {
    clearInterval(autoTimer);
    autoTimer = null;
    showMsg("Auto-actualización desactivada.");
  }
}

// eventos
btnCargar.addEventListener("click", handleCargar);
btnVaciar.addEventListener("click", handleVaciar);
chkAuto.addEventListener("change", handleAutoToggle);

// carga inicial (opcional: comentar si no quieres petición al abrir)
handleCargar();