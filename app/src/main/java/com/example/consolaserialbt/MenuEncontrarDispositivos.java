package com.example.consolaserialbt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.consolaserialbt.datos.DispositivosBTListAdapter;
import com.example.consolaserialbt.conexion.ServicioConexionBT;
import com.example.consolaserialbt.conexion.ServicioBT;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class MenuEncontrarDispositivos extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDispositivoBT;

    ListView lvDispositivosEncontrados;
    Button btnEncontrarDispositivos, btnConectarDispositivo;

    private ArrayList<BluetoothDevice> mDispositivosBTArray, mVacio;
    private DispositivosBTListAdapter mDispositivosBTListAdapter;

    Intent intent_ActivarVisibilidad;

    private List mArrayMACDispositivos;

    int repeticionEnvio = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_encontrar_dispositivos);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDispositivosBTArray = new ArrayList<>();
        mArrayMACDispositivos = new ArrayList<String>();

        btnEncontrarDispositivos = (Button) findViewById(R.id.btnRefrescar);
        lvDispositivosEncontrados = (ListView) findViewById(R.id.lvDispositivosEncontrados);

        btnEncontrarDispositivos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mBluetoothAdapter.isEnabled()) Toast.makeText(v.getContext(), "Active el Bluetooth y la visiblidad del dispositivo", Toast.LENGTH_SHORT).show();
                else
                {
                    mDispositivosBTArray.clear();
                    mDispositivosBTListAdapter = new DispositivosBTListAdapter(v.getContext(), R.layout.dispositivos_bt_list_adapter, mDispositivosBTArray);
                    lvDispositivosEncontrados.setAdapter(mDispositivosBTListAdapter);

                    if(mBluetoothAdapter.isDiscovering()) mBluetoothAdapter.cancelDiscovery();

                    try
                    { chequearPermisosBT(); }
                    catch (Exception e) { }

                    mBluetoothAdapter.startDiscovery();

                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mRegistrar_DispositivosBTCercanos, filter);
                }
            }
        });

        lvDispositivosEncontrados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //se cancela la busqueda de dispositivos
                mBluetoothAdapter.cancelDiscovery();
                try
                {
                    try { unregisterReceiver(mRegistrar_DispositivosBTCercanos); }
                    catch (Exception e) { }

                    Intent intent = new Intent(view.getContext(), ServicioBT.class);
                    intent.putExtra("dispositivoBT", mDispositivosBTArray.get(position));
                    startService(intent);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(MenuEncontrarDispositivos.this, "error estableciendo conexion", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        try
        {
            unregisterReceiver(mRegistrar_DispositivosBTCercanos);
            stopService(intent_ActivarVisibilidad);
        }
        catch (Exception e){}

    }

    private BroadcastReceiver mRegistrar_DispositivosBTCercanos = new BroadcastReceiver() {
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
    };

    private void chequearPermisosBT()
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
        {
            try
            {
                int chequeoPermisos = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                chequeoPermisos += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if(chequeoPermisos != 0) this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1001);
                else Toast.makeText(this, "La aplicacion no tiene permiso para ejecutar esta accion", Toast.LENGTH_SHORT).show();
            }catch(Exception e){}

        }
    }
}
