package com.example.consolaserialbt.conexion;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class ServicioConexionBT
{
    //TAG para debugear
    private static final String TAG = "BTServicioConexion";

    private final BluetoothAdapter mBluetoothAdapter;
    //private final BluetoothDevice mBluetoothDevice;

    Context mContext;

    private ServicioConexionBT.Hilo_Aceptar mHiloAceptarConexion;
    private ServicioConexionBT.Hilo_Conectar mHiloConectar;
    private ServicioConexionBT.Hilo_Conexion mHiloConexion;

    private BluetoothDevice mDispositivo;
    ProgressDialog mProgressDialog;

    //UUID UUID_INSEGURO = UUID.fromString("HC-05");
    UUID UUID_INSEGURO = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ServicioConexionBT(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        iniciar();
    }

    public ServicioConexionBT(Context context){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        iniciar();
    }

    //region PROCEDIMIENTOS INTERNOS DE LA CLASE

    /**
     * procedimiento que permite iniciar el hilo de aceptar conexion al principio
     * este es llamado
     *
     * LO CAMBIE A PRIVADO: ORIGINALMENTE ERA PUBLICO
     */
    private synchronized void iniciar()
    {
        if(mHiloConectar != null)
        {
            mHiloConectar.cancel();
            mHiloConectar = null;
        }
        if(mHiloAceptarConexion == null)
        {
            mHiloAceptarConexion = new ServicioConexionBT.Hilo_Aceptar();
            mHiloAceptarConexion.start();
        }
    }


    private void ComenzarHiloConexion(BluetoothSocket mmBTSocket)
    {
        mHiloConexion = new ServicioConexionBT.Hilo_Conexion(mmBTSocket);
        mHiloConexion.start();
    }


    private static void servicioRecibirDatos(Context context, String mensaje) {

        Intent intent = new Intent("Recibir por BT");
        intent.putExtra("mensaje", mensaje);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    // (-1) NO CONECTO
    // (0) INICIANDO CONEXION
    // (1) CONECTANDO
    // (2) CONECTADO
    // (3) DESCONECTAR
    private static void ConfirmacionConexion(Context context, int caso, String mensaje) {
        Intent intent = new Intent("BT Conectado");
        intent.putExtra("caso", caso);
        intent.putExtra("mensaje", mensaje);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    //endregion

    //region DEFINICION DE HILOS DE EJECUCION

    //region HILO_ACEPTAR

    /**
     * HILOS QUE NO SON EXPUESTOS POR LA CLASE PERO SON EMPLEADOS PARA
     *  1. ACEPTAR
     *  2. COMENZAR
     *  3. MANTENER
     *  LA COMUNICACION BLUETOOTH
     */
    private class Hilo_Aceptar extends Thread
    {
        private final BluetoothServerSocket mmBluetoothSeverSocket;

        public Hilo_Aceptar()
        {
            BluetoothServerSocket tmp = null;
            try
            {
                Log.d(TAG, "Comenzando socket de comunicacion sin encriptacion");

                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("appcebadero",UUID_INSEGURO);

                Log.d(TAG, "socket de comunicacion sin encriptacion aceptado");
                ConfirmacionConexion(mContext, 1, "Conectando...");
            }
            catch(Exception e){}

            mmBluetoothSeverSocket = tmp;
        }

        @Override
        public void run()
        {
            BluetoothSocket mmSocket = null;
            try
            {
                Log.d(TAG, "Hilo_Aceptar: run(): iniciando SOCKET RFCOMM...");
                mmSocket = mmBluetoothSeverSocket.accept();
                Log.d(TAG, "Hilo_Aceptar: run(): conexion con RFCOMM ACEPTADA!");
            }catch (Exception e)
            {
                Log.d(TAG, "Hilo_Aceptar: run(): EXCEPCION: " + e.getMessage());
            }

            if(!mmSocket.equals(null))
            {
                ComenzarHiloConexion(mmSocket);
            }
        }

        public void cancel()
        {
            try
            {
                mmBluetoothSeverSocket.close();
            }catch (Exception e){}
        }
    }
    //endregion

    //region HILO_CONEXION
    private class Hilo_Conexion extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmEntradaDatos;
        private final OutputStream mmSalidaDatos;

        public Hilo_Conexion(BluetoothSocket mmBTSocket)
        {
            Log.d(TAG, "Hilo_Conexion: iniciando hilo de conexion");
            mmSocket = mmBTSocket;
            InputStream tmpEntrada = null;
            OutputStream tmpSalida = null;
//            try
//            {
//                mProgressDialog.dismiss();
//            }catch (Exception e){}
            try
            {
                tmpEntrada = mmSocket.getInputStream();
                tmpSalida = mmSocket.getOutputStream();
            }catch (Exception e){}

            mmEntradaDatos = tmpEntrada;
            mmSalidaDatos = tmpSalida;
        }

        @Override
        public void run() {
            byte[] bufferDatos = new byte[1024];
            int bytes = 0;

            try
            {
                Thread.sleep(1000);
            }catch (Exception e){ }

            ConfirmacionConexion(mContext, 2, "Conectado");

            while(true)
            {
                //bloque que atrapa si existe una excepcion leyendo los datos del buffer
                //sin embargo NO ROMPE el BUCLE
                try
                {
                    bytes = mmEntradaDatos.read(bufferDatos);

                    String mensajeRecibido = new String(bufferDatos, 0, bytes);
                    Log.d(TAG, "ENTRADA DATOS: "+ mensajeRecibido);

                    servicioRecibirDatos(mContext, "$"+ mensajeRecibido);

                }catch (Exception e) {
                    Log.e(TAG,"Error leyendo los datos de entrada. " + e.getMessage());
                    break;
                }
            }
        }

        public void cancel()
        {
            if(mmSocket != null)
            {
                try
                {
                    mmSocket.close();
                }catch (Exception e){}
            }
            if(mmSalidaDatos != null)
            {
                try
                {
                    mmSalidaDatos.close();
                }catch (Exception e){}
            }
            if(mmEntradaDatos != null)
            {
                try
                {
                    mmEntradaDatos.close();
                }catch (Exception e){}
            }
        }

        public void escribir(String mensaje)
        {
            //String texto = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "Hilo_Conexion(): escribir: escribiendo en el buffer de salida: " + mensaje);
            try
            {
                mmSalidaDatos.write(mensaje.getBytes(Charset.defaultCharset()));
            }catch (Exception e)
            {
                Log.d(TAG, "Hilo_Conexion(): escribir: la escritura en el bufer salio. " + e.getMessage());
            }
        }
    }
    //endregion

    //region HILO_CONECTAR
    private class Hilo_Conectar extends Thread
    {
        private BluetoothSocket mmSocket;

        public Hilo_Conectar(BluetoothDevice mmDispositivo)
        {
            mDispositivo = mmDispositivo;
        }

        @Override
        public void run() {

            Log.d(TAG, "Hilo_Conectar(): run: comenzando la conexion a un dispositivo");
            BluetoothSocket tmp = null;
            try
            {
                Log.d(TAG, "Comenzando socket de comunicacion sin encriptacion");

                tmp = mDispositivo.createInsecureRfcommSocketToServiceRecord(UUID_INSEGURO);
//                Method method = mBluetoothAdapter.getClass().getMethod("listenUsingRfcommOn" , new Class[]{int.class});
//                tmp = (BluetoothSocket) method.invoke(mBluetoothAdapter, 1);

                Log.d(TAG, "socket de comunicacion sin encriptacion aceptado");
            }
            catch(Exception e){}

            mmSocket = tmp;
            mBluetoothAdapter.cancelDiscovery();

            try
            {
                mmSocket.connect();
                Log.d(TAG, "Hilo_Conectar(): run: conectando al socket...");

            }catch (Exception e)
            {
                try
                {
                    ConfirmacionConexion(mContext, 3, "");
                    Log.d(TAG, "Hilo_Conectar(): run: fallo al crear el socket, cerrando...");
                    mmSocket.close();
                }catch (Exception ei)
                {
                    Log.d(TAG, "Hilo_Conectar(): run: error cerrando el socket...");
                }
            }

            if(!mmSocket.equals(null))
            {
                ComenzarHiloConexion(mmSocket);
            }
        }

        public void cancel()
        {
            try
            {
                Log.d(TAG, "Hilo_Conectar(): cancel: Cerrando Socket Cliente:");
                mmSocket.close();
            }catch (Exception e)
            {
                Log.d(TAG, "Hilo_Conectar(): cancel: error cerrando el socket de cliente: " + e.getMessage());
            }
        }
    }
    //endregion


    //endregion

    //region PROCEDIMIENTOS EXPUESTOS POR LA CLASE

    public void comenzarCliente(BluetoothDevice dispositivoBT)
    {
//        mProgressDialog = ProgressDialog.show(
//                mContext,
//                "Conectando Bluetooth",
//                "Porfavor espere...",
//                true);

        mHiloConectar = new ServicioConexionBT.Hilo_Conectar(dispositivoBT);
        mHiloConectar.start();
    }

    public void escribir(String salida)
    {
        mHiloConexion.escribir(salida);
    }

    public void detenerHilos()
    {
        try
        {
            mHiloConexion.cancel();
            mHiloConexion.interrupt();
        }catch(Exception e){}
        try
        {
            //Thread.sleep(1000);
            mHiloConectar.cancel();
            mHiloConectar.interrupt();
        }catch(Exception e){}
    }

    //endregion

}
