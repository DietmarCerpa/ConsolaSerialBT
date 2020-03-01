package com.example.consolaserialbt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.consolaserialbt.datos.DatosSerialesListAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    Button btnBuscarBT, btnDesconectarBT, btnLimpiarLog;
    ImageView ivEnviarTexto;
    EditText etTextoAEnviar;
    ListView lvDatosSerial;

    BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> mDatosArray, mVacio;
    private DatosSerialesListAdapter mDatosSerialesListAdapter;

    private int estadoConexionBT = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //Se inicializa el valor de las preferencias compartidas para evitar conectarnos con un dispositivo anterior
        //LimpiarPreferenciasCompartidas();

        btnBuscarBT = (Button) findViewById(R.id.btnBuscarBT);
        btnDesconectarBT = (Button) findViewById(R.id.btnDesconectarBT);
        btnLimpiarLog = (Button) findViewById(R.id.btnLimpiarLog);
        ivEnviarTexto = (ImageView) findViewById(R.id.ivEnviarTexto);
        etTextoAEnviar = (EditText) findViewById(R.id.etTextoAEnviar);
        lvDatosSerial = (ListView) findViewById(R.id.lvDatosSerial);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mDatosArray = new ArrayList<>();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRecibirDatoBT, new IntentFilter("RX_BT"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mEstadoServicio, new IntentFilter("ESTADO_SERVICIO"));

        //se revisa si el dispositivo posee un modulo bluetooth
        if (mBluetoothAdapter.equals(null))
            Toast.makeText(this, "Este dispositivo no es compatible con la tecnologia Bluetooth", Toast.LENGTH_SHORT).show();
        if (!mBluetoothAdapter.isEnabled()) //validacion para activar el modulo
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }

        btnBuscarBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MenuEncontrarDispositivos.class);
                startActivity(intent);
            }
        });

        btnDesconectarBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ServicioBT(view.getContext(), 1);
            }
        });

        ivEnviarTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!etTextoAEnviar.getText().equals(""))
                {
                    if(estadoConexionBT == 2)
                    {
                        EscribirDatoListView(etTextoAEnviar.getText().toString());
                        EnviarDatoBT(view.getContext(), etTextoAEnviar.getText().toString() + System.getProperty("line.separator").toString());
                    }
                    else Toast.makeText(MainActivity.this, "Aun no estas conectado a un dispositivo bluetooth.", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(MainActivity.this, "Debes escribir al menos un caracter.", Toast.LENGTH_SHORT).show();

            }
        });

        btnLimpiarLog.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                mDatosArray.clear();
                mDatosSerialesListAdapter = new DatosSerialesListAdapter(view.getContext(), R.layout.datos_seriales_list_adapter, mDatosArray);
                lvDatosSerial.setAdapter(mDatosSerialesListAdapter);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRecibirDatoBT);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mEstadoServicio);
        } catch (Exception e) { }
    }

    private void EscribirDatoListView(String mensaje)
    {
        mDatosArray.add(mensaje);
        mDatosSerialesListAdapter = new DatosSerialesListAdapter(this, R.layout.datos_seriales_list_adapter, mDatosArray);
        lvDatosSerial.setAdapter(mDatosSerialesListAdapter);
    }

    private static void ServicioBT(Context context, int caso)
    {
        Intent intent = new Intent("SERVICIO_BT");
        intent.putExtra("caso", caso);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void EnviarDatoBT(Context context, String mensaje) {
        Intent intent = new Intent("TX_BT");
        intent.putExtra("mensaje", mensaje);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private BroadcastReceiver mRecibirDatoBT = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mensaje = "$" + (String) intent.getExtras().get("mensaje");

            EscribirDatoListView(mensaje);
        }
    };

    private BroadcastReceiver mEstadoServicio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int caso = (int) intent.getExtras().get("caso");
            String mensaje ="¡" + (String) intent.getExtras().get("mensaje");

            if(caso == 0 && estadoConexionBT != caso)
            {
                EscribirDatoListView(mensaje);
                estadoConexionBT = caso;
            }
            else if (caso == 1 && estadoConexionBT != caso)
            {
                EscribirDatoListView(mensaje);
                estadoConexionBT = caso;
            }
            else if (caso == 2 && estadoConexionBT != caso)
            {
                EscribirDatoListView(mensaje);
                estadoConexionBT = caso;
            }
            else if (caso == 3 && estadoConexionBT != caso)
            {
                EscribirDatoListView(mensaje);
                ServicioBT(context, 1);
            }

        }
    };
}
