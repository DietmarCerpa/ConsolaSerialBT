package com.example.consolaserialbt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
    Button btnBuscarBT, btnDesconectarBT;
    ImageView ivEnviarTexto;
    EditText etTextoAEnviar;
    ListView lvDatosSerial;

    BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> mDatosArray, mVacio;
    private DatosSerialesListAdapter mDatosSerialesListAdapter;

    private int estadoConexionBT;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //Se inicializa el valor de las preferencias compartidas para evitar conectarnos con un dispositivo anterior
        //LimpiarPreferenciasCompartidas();

        btnBuscarBT = (Button) findViewById(R.id.btnBuscarBT);
        btnDesconectarBT = (Button) findViewById(R.id.btnDesconectarBT);
        ivEnviarTexto = (ImageView) findViewById(R.id.ivEnviarTexto);
        etTextoAEnviar = (EditText) findViewById(R.id.etTextoAEnviar);
        lvDatosSerial = (ListView) findViewById(R.id.lvDatosSerial);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mDatosArray = new ArrayList<>();

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
                comunicacionServicio(view.getContext(), 1);
            }
        });

        ivEnviarTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!etTextoAEnviar.getText().equals(""))
                {
                    mDatosArray.add(etTextoAEnviar.getText().toString());
                    mDatosSerialesListAdapter = new DatosSerialesListAdapter(view.getContext(), R.layout.datos_seriales_list_adapter, mDatosArray);
                    lvDatosSerial.setAdapter(mDatosSerialesListAdapter);
                }

                /*mDispositivosBTListAdapter = new DispositivosBTListAdapter(v.getContext(), R.layout.dispositivos_bt_list_adapter, mDispositivosBTArray);
                lvDispositivosEncontrados.setAdapter(mDispositivosBTListAdapter);*/

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            LocalBroadcastManager.getInstance(this).registerReceiver(mConfirmacionConexion, new IntentFilter("BT Conectado"));
            LocalBroadcastManager.getInstance(this).registerReceiver(mRecibirDatoBT, new IntentFilter("Recibir por BT"));
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRecibirDatoBT);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mConfirmacionConexion);
        } catch (Exception e) {
        }

    }

    private void EnviarDatosSerial(Context context, String mensaje)
    {
        mDatosArray.add(mensaje);
        mDatosSerialesListAdapter = new DatosSerialesListAdapter(context, R.layout.datos_seriales_list_adapter, mDatosArray);
        lvDatosSerial.setAdapter(mDatosSerialesListAdapter);
    }


/*    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences datos = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String MAC_BT = datos.getString("MAC_BT", "");

        if(MAC_BT.isEmpty() || MAC_BT.equals("null"))
            Toast.makeText(this, "Null o Vacio", Toast.LENGTH_SHORT).show();

        *//*SharedPreferences datosCompartidos = PreferenceManager.getDefaultSharedPreferences(MenuSelector.this);
        String id_Dispositivo = datosCompartidos.getString("Id_BT","");
        String MAC_Dispositivo = datosCompartidos.getString("MAC_BT", "");*//*
    }*/

/*
    private void LimpiarPreferenciasCompartidas() {
        SharedPreferences datos = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = datos.edit();
        editor.putString("MAC_BT", null);
        editor.putString("NOMBRE_BT", null);
        editor.putString("DISPOSITIVO_BT", null);
        editor.apply();
    }*/

/*

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRecibirDatoBT);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mConfirmacionConexion);
        }catch(Exception e){}
    }
*/

    private static void comunicacionServicio(Context context, int caso)
    {
        Intent intent = new Intent("CSERVICIO");
        intent.putExtra("mensaje", caso);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void servicioEnviarDatos(Context context, String mensaje) {
        Intent intent = new Intent("Enviar por BT");
        intent.putExtra("mensaje", mensaje);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private BroadcastReceiver mConfirmacionConexion = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int caso = (int) intent.getExtras().get("caso");
                String mensaje = (String) intent.getExtras().get("mensaje");
                switch (caso)
                {
                    case -1:
                        break;
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;

//                    case 0:
//                        Toast.makeText(context, "Conectando", Toast.LENGTH_SHORT).show();
//                        break;
//                    case 1:
////                        servicioEnviarDatos(context, "#");
//                        Toast.makeText(context, "se ha conectado correctamente", Toast.LENGTH_SHORT).show();
//                        break;
//                    default:
//                        Toast.makeText(context, "error de conexion", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
            }
        }
    };

    private BroadcastReceiver mRecibirDatoBT = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mensaje = (String) intent.getExtras().get("mensaje");
            //TODO: mostrar datos en LIST VIEW
        }
    };

        private void CerrarConexionBT(Context context) {
            servicio_Configuracion(context, 1);
            retardo_500ms();
            servicio_Configuracion(context, 2);
            retardo_500ms();
        }

        private static void servicio_Configuracion(Context context, int mensaje) {
            Intent intent = new Intent("CSERVICIO");
            intent.putExtra("mensaje", mensaje);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        private void retardo_500ms() {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }

/*    private BroadcastReceiver mRegistrar_DispositivosBTCercanos = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String accion = intent.getAction();
            if(accion.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(!mArrayMACDispositivos.isEmpty())
                {
                    for(int i=0; i < mArrayMACDispositivos.size(); i++)
                        if(!mArrayMACDispositivos.get(i).toString().equals(dispositivo.getAddress()))
                            mDispositivosBTArray.add(dispositivo);
                }
                else mDispositivosBTArray.add(dispositivo);
                mDispositivosBTListAdapter = new DispositivosBTListAdapter(context, R.layout.dispositivos_bt_list_adapter, mDispositivosBTArray);
                lvDispositivosEncontrados.setAdapter(mDispositivosBTListAdapter);
            }
        }
    };*/
}
