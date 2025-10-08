package com.example.jbperfon.android_app;

import android.util.Log;

public class LogicaFake {

    private static final String TAG = "LogicaFake";

    public void postMedicion(String json, String host, String path) {
        if (json == null || json.trim().isEmpty()) {
            Log.w(TAG, "postMedicion(): JSON vacío, no se envía");
            return;
        }
        final String url = (host.endsWith("/") ? host.substring(0, host.length() - 1) : host)
                + (path.startsWith("/") ? path : "/" + path);

        Log.d(TAG, "POST -> " + url);
        Log.d(TAG, "Body -> " + json);

        PeticionarioREST http = new PeticionarioREST();
        http.hacerPeticionREST("POST", url, json, (codigo, cuerpo) -> {
            Log.d(TAG, "Respuesta (" + codigo + "): " + cuerpo);
        });
    }
}