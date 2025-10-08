package com.example.jbperfon.android_app;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * PeticionarioREST
 * Cliente HTTP muy simple basado en AsyncTask para ejecutar peticiones en background.
 * - Admite GET/POST/PUT/DELETE (lo que le pases en 'metodo').
 * - Si 'cuerpo' no es null y el método no es GET, lo envía como JSON (UTF-8).
 * - Devuelve en el callback el código HTTP y el cuerpo de la respuesta (si existe).
 *
 * NOTA: AsyncTask está deprecado en APIs modernas, pero vale para ejercicios/clases.
 * En producción se recomienda WorkManager, coroutines/Kotlin, Retrofit, etc.
 */
public class PeticionarioREST extends AsyncTask<Void, Void, Boolean> {

    public interface RespuestaREST {
        void callback(int codigo, String cuerpo);
    }

    private static final String TAG = "PeticionarioREST";

    // Parámetros de la petición
    private String metodoHttp;
    private String urlObjetivo;
    private String cuerpoAEnviar;           // puede ser null
    private RespuestaREST receptorRespuesta;

    // Resultado
    private int statusCode = -1;
    private String respuestaTexto = "";

    // API pública para lanzar la petición
    public void hacerPeticionREST(String metodo, String urlDestino, String cuerpo, RespuestaREST callback) {
        this.metodoHttp = (metodo == null ? "GET" : metodo.trim().toUpperCase());
        this.urlObjetivo = urlDestino;
        this.cuerpoAEnviar = cuerpo;
        this.receptorRespuesta = callback;

        execute(); // ejecuta doInBackground() en otro hilo
    }

    @Override
    protected Boolean doInBackground(Void... ignore) {
        HttpURLConnection conn = null;
        try {
            Log.d(TAG, "Conectando a: " + urlObjetivo + " [" + metodoHttp + "]");

            URL url = new URL(urlObjetivo);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(metodoHttp);
            conn.setConnectTimeout(8000); // 8s
            conn.setReadTimeout(12000);   // 12s
            conn.setDoInput(true);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Enviar cuerpo si corresponde (no GET y hay cuerpo)
            boolean enviaCuerpo = !"GET".equalsIgnoreCase(metodoHttp) && cuerpoAEnviar != null;
            if (enviaCuerpo) {
                conn.setDoOutput(true);
                byte[] payload = cuerpoAEnviar.getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(payload.length);
                try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                    dos.write(payload);
                    dos.flush();
                }
            }

            // Lanzar petición
            statusCode = conn.getResponseCode();
            Log.d(TAG, "HTTP " + statusCode + " " + conn.getResponseMessage());

            // Elegir inputStream correcto (body o error)
            InputStream is;
            if (statusCode >= 200 && statusCode < 300) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream(); // importante para leer errores
            }

            if (is != null) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        sb.append(linea);
                    }
                }
                respuestaTexto = sb.toString();
                Log.d(TAG, "Cuerpo recibido (" + respuestaTexto.length() + " bytes)");
            } else {
                respuestaTexto = "";
                Log.d(TAG, "Sin cuerpo en la respuesta");
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Excepción en petición: " + e.getMessage(), e);
            respuestaTexto = "";
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean ok) {
        Log.d(TAG, "Terminado. OK=" + ok + " Código=" + statusCode);
        if (receptorRespuesta != null) {
            receptorRespuesta.callback(statusCode, respuestaTexto);
        }
    }
}