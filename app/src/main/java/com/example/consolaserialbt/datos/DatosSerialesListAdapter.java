package com.example.consolaserialbt.datos;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.consolaserialbt.R;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DatosSerialesListAdapter extends ArrayAdapter<String>
{

    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mDatos;
    private int mRevisarIdRecurso;

    public DatosSerialesListAdapter(Context context, int tvIDRecurso, ArrayList<String> mmDatos)
    {
        super(context, tvIDRecurso, mmDatos);
        this.mDatos = mmDatos;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRevisarIdRecurso = tvIDRecurso;
    }

    public View getView(int posicion, View convertView, ViewGroup parent)
    {
        convertView = mLayoutInflater.inflate(mRevisarIdRecurso, null);
        String datos = mDatos.get(posicion);


        TextView tvEntradaSalida = (TextView) convertView.findViewById(R.id.tvEntradaSalida);
        TextView tvMensaje = (TextView) convertView.findViewById(R.id.tvMensaje);

        if(datos.startsWith("$"))
        {
            tvEntradaSalida.setText("RECIBIDO: ");
            tvMensaje.setText(datos.substring(1));
        }
        else if(datos.startsWith("¡"))
        {
            tvEntradaSalida.setText("SISTEMA: ");
            tvMensaje.setText("Conectando....");
        }
        else
        {
            tvEntradaSalida.setText("ENVIADO: ");
            tvMensaje.setText(datos);
        }
        return convertView;
    }

    /*

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
    }*/
}

