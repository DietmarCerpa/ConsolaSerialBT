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
            LocalBroadcastManager.getInstance(this).registerReceiver(mComunicacionServicio, new IntentFilter("CSERVICIO"));

            mBluetoothDevice = (BluetoothDevice) intent.getExtras().get("dispositivoBT"); //obtiene el dispositivo del intent convocado

            Log.d(TAG, "Servicio iniciado... dispositivo: " + mBluetoothDevice);


            mServicioConexionBT = new ServicioConexionBT(this);
            Log.d(TAG, "comenzando comunicacion..."); //se crea una nueva clase dependiente de ServicioConexionBT.java

            mServicioConexionBT.comenzarCliente(mBluetoothDevice);
            Log.d(TAG, "comunicacion establecida");

            //mensajeServicio(this, CONECTADO);
            ESTADO = CONECTADO;

            return START_NOT_STICKY;
        }catch (Exception e)
        {
            //mensajeServicio(this, NO_CONECTADO);
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
        {
            ESTADO = NO_CONECTADO;
        }catch (Exception e){}
        stopForeground(true);
    }

    private BroadcastReceiver mEnviarADatoBT = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mensaje = (String) intent.getExtras().get("mensaje");
            Log.d(TAG, "mensaje recibido: " + mensaje);
            mServicioConexionBT.escribir(mensaje);
        }
    };


    private static void ConfirmacionConexion(Context context, int caso, String mensaje) {
        Intent intent = new Intent("BT Conectado");
        intent.putExtra("caso", caso);
        intent.putExtra("mensaje", mensaje);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private BroadcastReceiver mComunicacionServicio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int mensaje = (int) intent.getExtras().get("mensaje");

            switch (mensaje)
            {
                case 1:
                    mServicioConexionBT.detenerHilos();
                    break;
                case 2:
                    LocalBroadcastManager.getInstance(ServicioBT.this).unregisterReceiver(mEnviarADatoBT);
                    LocalBroadcastManager.getInstance(ServicioBT.this).unregisterReceiver(mComunicacionServicio);
                    stopSelf();
                    break;
            }
        }
    };


}
