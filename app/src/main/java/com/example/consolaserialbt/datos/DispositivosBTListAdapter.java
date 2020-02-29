package com.example.consolaserialbt.datos;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.consolaserialbt.R;

import java.util.ArrayList;

public class DispositivosBTListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDispositivos;
    private int mRevisarIdRecurso;

    public DispositivosBTListAdapter(Context context, int tvIDRecurso, ArrayList<BluetoothDevice> mmDispositivos)
    {
        super(context, tvIDRecurso, mmDispositivos);
        this.mDispositivos = mmDispositivos;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRevisarIdRecurso = tvIDRecurso;
    }

    public View getView(int posicion, View convertView, ViewGroup parent)
    {
        convertView = mLayoutInflater.inflate(mRevisarIdRecurso, null);
        BluetoothDevice dispositivoBT = mDispositivos.get(posicion);

        if(!dispositivoBT.equals(null))
        {
            TextView nombreDispositivo = (TextView) convertView.findViewById(R.id.tvNombreDispositivo);
            TextView direccionDispositivo = (TextView) convertView.findViewById(R.id.tvDireccionMac);

            if(!nombreDispositivo.equals(null) && !nombreDispositivo.getText().equals("null")) nombreDispositivo.setText("Nombre: " + dispositivoBT.getName());
            else nombreDispositivo.setText("Nombre: oculto");
            if(!direccionDispositivo.equals(null) && !direccionDispositivo.getText().equals("null")) direccionDispositivo.setText("MAC: " + dispositivoBT.getAddress());

        }
        return convertView;
    }
}
