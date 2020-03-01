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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        if(datos.startsWith("$"))
        {
            tvEntradaSalida.setText("RECIBIDO: " + date);
            tvMensaje.setText(datos.substring(1));
        }
        else if(datos.startsWith("¡"))
        {
            tvEntradaSalida.setText("SISTEMA: " + date);
            tvMensaje.setText(datos.substring(1));
        }
        else
        {
            tvEntradaSalida.setText("ENVIADO: " + date);
            tvMensaje.setText(datos);
        }
        return convertView;
    }
}

