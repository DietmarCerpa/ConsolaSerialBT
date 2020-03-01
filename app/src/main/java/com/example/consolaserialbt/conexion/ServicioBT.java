package com.example.consolaserialbt.conexion;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ServicioBT extends Service
{

    public static final String TAG = "appce_ServicioBT";
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    ServicioConexionBT mServicioConexionBT;

    public static final int CONECTADO = 1;
    public static final int NO_CONECTADO = 0;
    public int ESTADO;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        try
        {
            LocalBroadcastManager.getInstance(this).registerReceiver(mEnviarADatoBT, new IntentFilter("TX_BT"));
            LocalBroadcastManager.getInstance(this).registerReceiver(mServicioBT, new IntentFilter("SERVICIO_BT"));

            mBluetoothDevice = (BluetoothDevice) intent.getExtras().get("dispositivoBT"); //obtiene el dispositivo del intent convocado

            Log.d(TAG, "Servicio iniciado... dispositivo: " + mBluetoothDevice);

            EstadoServicio(this, 0,"Iniciando conexion con: " +  mBluetoothDevice.getAddress());

            mServicioConexionBT = new ServicioConexionBT(this);

            Log.d(TAG, "comenzando comunicacion..."); //se crea una nueva clase dependiente de ServicioConexionBT.java

            mServicioConexionBT.comenzarCliente(mBluetoothDevice);
            Log.d(TAG, "comunicacion establecida");

            ESTADO = CONECTADO;
            return START_NOT_STICKY;
        }catch (Exception e)
        {
            ESTADO = NO_CONECTADO;
            return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        ESTADO = NO_CONECTADO;
        Log.d("bluetooth", "ServicioBT Creado");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Servicio destruido...");
        try
        { ESTADO = NO_CONECTADO; }
        catch (Exception e){}
        stopForeground(true);
    }

    // (-1) NO CONECTO
    // (0) INICIANDO CONEXION
    // (1) CONECTANDO
    // (2) CONECTADO
    // (3) DESCONECTAR
    private static void EstadoServicio(Context context, int caso, String mensaje) {
        Intent intent = new Intent("ESTADO_SERVICIO");
        intent.putExtra("caso", caso);
        intent.putExtra("mensaje", mensaje);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private BroadcastReceiver mEnviarADatoBT = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mensaje = (String) intent.getExtras().get("mensaje");
            Log.d(TAG, "mensaje recibido: " + mensaje);
            mServicioConexionBT.escribir(mensaje);
        }
    };

    private BroadcastReceiver mServicioBT = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int caso = (int) intent.getExtras().get("caso");

            switch (caso)
            {
                case 1:
                    mServicioConexionBT.detenerHilos();
                    LocalBroadcastManager.getInstance(ServicioBT.this).unregisterReceiver(mEnviarADatoBT);
                    LocalBroadcastManager.getInstance(ServicioBT.this).unregisterReceiver(mServicioBT);
                    stopSelf();
                    break;
            }
        }
    };
}
