package com.example.jbperfon.android_app;
// ------------------------------------------------------------------
// ------------------------------------------------------------------

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

// ------------------------------------------------------------------
// ------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    private static final int REQ_ENABLE_BT = 1001;
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    private TramaIBeacon ultimoBeacon = null;

    private int ultimoRssi = 0;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE( resultado );
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        // ----> MOD: si faltan permisos, los pedimos (según versión) en vez de 'return'
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{ Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT },
                        CODIGO_PETICION_PERMISOS
                );
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{ Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION },
                        CODIGO_PETICION_PERMISOS
                );
                return;
            }
        }

        this.elEscanner.startScan( this.callbackDelEscaneo);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void mostrarInformacionDispositivoBTLE( ScanResult resultado ) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = (resultado.getScanRecord() != null) ? resultado.getScanRecord().getBytes() : null;
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");

        // ----> MOD: si falta CONNECT, lo pedimos (en 12+), en 9–11 no existe y no hace falta pedirlo aquí
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{ Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN },
                        CODIGO_PETICION_PERMISOS
                );
                return;
            }
        }

        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi );

        if (bytes != null) {
            Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
            Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

            TramaIBeacon tib = new TramaIBeacon(bytes);

            Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
            Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
            Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
            Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
            Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
            Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
            Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                    + tib.getiBeaconLength() + " ) ");
            Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
            Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
            Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                    + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
            Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                    + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
            Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
            Log.d(ETIQUETA_LOG, " ****************************************************");
        } else {
            Log.d(ETIQUETA_LOG, " ScanRecord nulo: no hay bytes para mostrar.");
        }

    } // ()

    // --------------------------------------------------------------
// --------------------------------------------------------------
    
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado ) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");

        // si ya hay un escaneo activo, lo paramos antes de arrancar otro
        if (this.callbackDelEscaneo != null && this.elEscanner != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    this.elEscanner.stopScan(this.callbackDelEscaneo);
                }
            } else {
                this.elEscanner.stopScan(this.callbackDelEscaneo);
            }
        }

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");
                mostrarInformacionDispositivoBTLE(resultado);
                if (resultado.getScanRecord() != null && resultado.getScanRecord().getBytes() != null) {
                    byte[] bytes = resultado.getScanRecord().getBytes();
                    try {
                        ultimoBeacon = new TramaIBeacon(bytes);  // <--- aquí creamos el objeto
                        String json = construirJsonMedicion(ultimoBeacon, ultimoRssi);
                        Log.d(ETIQUETA_LOG, "JSON de medición = " + json);

                        //TESTS RÁPIDOS

                        // Opción A: probar sin backend, con mock público (HTTPS)
                        new LogicaFake().postMedicion(json, "https://httpbin.org", "/post");

// Opción B: backend local FastAPI (emulador Android -> host)
                        new LogicaFake().postMedicion(json, "http://10.236.32.34:8000", "/api/v1/mediciones");


                        ultimoRssi = resultado.getRssi();

                        Log.d(ETIQUETA_LOG, "iBeacon guardado -> " +
                                "uuid=" + Utilidades.bytesToString(ultimoBeacon.getUUID()) +
                                " major=" + Utilidades.bytesToInt(ultimoBeacon.getMajor()) +
                                " minor=" + Utilidades.bytesToInt(ultimoBeacon.getMinor()) +
                                " tx=" + ultimoBeacon.getTxPower() +
                                " rssi=" + ultimoRssi);
                        // (opcional) parar el escaneo al encontrarlo

                        if (elEscanner != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN)
                                        == PackageManager.PERMISSION_GRANTED) {
                                    elEscanner.stopScan(callbackDelEscaneo);
                                }
                            } else {
                                elEscanner.stopScan(callbackDelEscaneo);
                            }
                        }

                    } catch (Exception e) {
                        Log.w(ETIQUETA_LOG, "Error creando TramaIBeacon: " + e.getMessage());
                    }
                } else {
                    Log.d(ETIQUETA_LOG, "ScanRecord nulo: no se puede construir TramaIBeacon.");
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() code=" + errorCode);
            }
        };

        // Filtro por NOMBRE EXACTO del dispositivo
        ScanFilter filtroPorNombre = new ScanFilter.Builder()
                .setDeviceName(dispositivoBuscado)
                .build();
        java.util.ArrayList<ScanFilter> filtros = new java.util.ArrayList<>();
        filtros.add(filtroPorNombre);

        // Ajustes de escaneo (rápido)
        android.bluetooth.le.ScanSettings ajustes = new android.bluetooth.le.ScanSettings.Builder()
                .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado);

        // Permisos según versión (pedir si faltan y salir)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        CODIGO_PETICION_PERMISOS
                );
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
                        CODIGO_PETICION_PERMISOS
                );
                return;
            }
        }

        // ¡Ahora sí: escaneo con FILTRO por nombre!
        this.elEscanner.startScan(filtros, ajustes, this.callbackDelEscaneo);
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
// Convierte la trama iBeacon a un JSON sencillo de medición.
// Usa el 'minor' como medida (puedes cambiarlo si quieres).
    private String construirJsonMedicion(TramaIBeacon tib, int rssi) {
        if (tib == null) return "{}";
        try {
            int medida = Utilidades.bytesToInt(tib.getMinor());
            long ts = System.currentTimeMillis();
            // JSON compacto (sin librerías externas)
            return "{\"medida\":" + medida + ",\"rssi\":" + rssi + ",\"ts\":" + ts + "}";
        } catch (Exception e) {
            Log.w(ETIQUETA_LOG, "construirJsonMedicion(): error creando JSON: " + e.getMessage());
            return "{}";
        }
    }
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {

        if ( this.callbackDelEscaneo == null ) {
            return;
        }

        // ----> MOD: si faltan permisos, pedirlos en vez de 'return'
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{ Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT },
                        CODIGO_PETICION_PERMISOS
                );
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{ Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION },
                        CODIGO_PETICION_PERMISOS
                );
                return;
            }
        }

        this.elEscanner.stopScan( this.callbackDelEscaneo );
        this.callbackDelEscaneo = null;

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado" );
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-PROY-3A" ) );

        //this.buscarEsteDispositivoBTLE( "EPSG-GTI-PROY-3A" );
        this.buscarEsteDispositivoBTLE( "JuanBan" );

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado" );
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        // Mejor vía: BluetoothManager (evita nulls en algunos dispositivos)
        android.bluetooth.BluetoothManager bm =
                (android.bluetooth.BluetoothManager) getSystemService(android.content.Context.BLUETOOTH_SERVICE);

        BluetoothAdapter bta = (bm != null) ? bm.getAdapter() : BluetoothAdapter.getDefaultAdapter();

        if (bta == null) {
            Log.e(ETIQUETA_LOG, " inicializarBlueTooth(): NO hay adaptador Bluetooth en este dispositivo/emulador");
            // Aquí puedes mostrar un Toast si quieres
            // Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show();
            return; // ← evita NPE
        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado inicial, enabled=" + bta.isEnabled());

        // Android 12+ necesita permisos antes de consultar ciertas APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{ Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN },
                        CODIGO_PETICION_PERMISOS
                );
                return;
            }
        }

        // En lugar de bta.enable() (deprecated / restringido), pide al usuario activarlo:
        if (!bta.isEnabled()) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Bluetooth APAGADO -> solicitamos activación");
            android.content.Intent intent = new android.content.Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_BT); // tras volver, seguimos
            return;
        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState());
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if (this.elEscanner == null) {
            Log.w(ETIQUETA_LOG, " inicializarBlueTooth(): NO se pudo obtener el escáner BLE (¿emulador sin BLE?)");
            return;
        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a pedir permisos (si no los tuviera) !!!!");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{ Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT },
                        CODIGO_PETICION_PERMISOS
                );
            } else {
                Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): ya tengo permisos BLE (12+)");
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{ Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION },
                        CODIGO_PETICION_PERMISOS
                );
            } else {
                Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");
            }
        }
    } // ()
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        inicializarBlueTooth();

    } // onCreate()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // ----> MOD: validar que TODOS los permisos salieron concedidos
                boolean todosOk = true;
                if (grantResults.length == 0) todosOk = false;
                for (int r : grantResults) {
                    if (r != PackageManager.PERMISSION_GRANTED) { todosOk = false; break; }
                }

                if (todosOk) {
                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                }  else {
                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos NO concedidos  !!!!");
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()

} // class
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
