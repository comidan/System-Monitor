package com.dev.system.monitor;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import it.gmariotti.cardslib.library.view.CardView;

public class SensorManagement extends Fragment implements SensorEventListener
{
    private View rootView;
    private Activity mainActivity;
    private GeneralInfoCard lightCard,gravityCard,accelerometerCard,gyroscopeCard,magneticFieldCard,pressureCard;
    private SensorManager sensorManager;
    private Sensor light,gravity,accelerometer,gyroscope,magnetic_field,pressure;
    private ArrayList<TextView>[] infoTextViews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.fragment_sensors, container, false);
        mainActivity=getActivity();
        sensorManager=(SensorManager)mainActivity.getSystemService(Context.SENSOR_SERVICE);
        light=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        gravity=sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope=sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetic_field=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        pressure=sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        infoTextViews=new ArrayList[6];
        ArrayList<String> info=new ArrayList<String>(),value=new ArrayList<String>();
        if(light!=null)
        {
            info.clear();
            value.clear();
            info.add(getString(R.string.light_sens));
            value.add("Loading");
            initCard(info,value,lightCard,getString(R.string.light),0,R.id.card_sensors_light);
        }
        else
            ((CardView)rootView.findViewById(R.id.card_sensors_light)).setVisibility(View.GONE);
        if(gravity!=null)
        {
            info.clear();
            value.clear();
            info.add(getString(R.string.x));
            value.add("Loading");
            info.add(getString(R.string.y));
            value.add("Loading");
            info.add(getString(R.string.z));
            value.add("Loading");
            initCard(info,value,gravityCard,getString(R.string.gravity),1,R.id.card_sensors_gravity);
        }
        else
            ((CardView)rootView.findViewById(R.id.card_sensors_gravity)).setVisibility(View.GONE);
        if(accelerometer!=null)
        {
            info.clear();
            value.clear();
            info.add(getString(R.string.x));
            value.add("Loading");
            info.add(getString(R.string.y));
            value.add("Loading");
            info.add(getString(R.string.z));
            value.add("Loading");
            initCard(info, value, accelerometerCard,getString(R.string.acceleration), 2, R.id.card_sensors_accelerometer);
        }
        else
            ((CardView)rootView.findViewById(R.id.card_sensors_accelerometer)).setVisibility(View.GONE);
        if(gyroscope!=null)
        {
            info.clear();
            value.clear();
            info.add(getString(R.string.x));
            value.add("Loading");
            info.add(getString(R.string.y));
            value.add("Loading");
            info.add(getString(R.string.z));
            value.add("Loading");
            initCard(info,value,gyroscopeCard,getString(R.string.gyroscope),3,R.id.card_sensors_gyroscope);
        }
        else
            ((CardView)rootView.findViewById(R.id.card_sensors_gyroscope)).setVisibility(View.GONE);
        if(magnetic_field!=null)
        {
            info.clear();
            value.clear();
            info.add(getString(R.string.x));
            value.add("Loading");
            info.add(getString(R.string.y));
            value.add("Loading");
            info.add(getString(R.string.z));
            value.add("Loading");
            initCard(info,value,magneticFieldCard,getString(R.string.magnetic_field),4,R.id.card_sensors_magnetic);
        }
        else
            ((CardView)rootView.findViewById(R.id.card_sensors_magnetic)).setVisibility(View.GONE);
        if(pressure!=null)
        {
            info.clear();
            value.clear();
            info.add(getString(R.string.pressure_sens));
            value.add("Loading");
            initCard(info,value,pressureCard,getString(R.string.pressure),5,R.id.card_sensors_pressure);
        }
        else
            ((CardView)rootView.findViewById(R.id.card_sensors_pressure)).setVisibility(View.GONE);
        mainActivity.setTitle(getString(R.string.sensor));
        return rootView;
    }

    private void initCard(ArrayList<String> info, ArrayList<String> value,GeneralInfoCard card,String cardTitle,int index,int id) {
        card=new GeneralInfoCard(mainActivity,info,value,cardTitle);
        card.init();
        CardView cardView = (CardView) rootView.findViewById(id);
        cardView.setCard(card);
        infoTextViews[index]=card.getTextViews();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        if(event.sensor.getType()==Sensor.TYPE_LIGHT&&light!=null)
            infoTextViews[0].get(0).setText(event.values[0]+" lx");
        else if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER&&accelerometer!=null)
        {
            infoTextViews[2].get(0).setText(new DecimalFormat("#.##").format(event.values[0])+" m/s²");
            infoTextViews[2].get(1).setText(new DecimalFormat("#.##").format(event.values[0])+" m/s²");
            infoTextViews[2].get(2).setText(new DecimalFormat("#.##").format(event.values[0])+" m/s²");
        }
        else if(event.sensor.getType()==Sensor.TYPE_GRAVITY&&gravity!=null)
        {
            infoTextViews[1].get(0).setText(new DecimalFormat("#.##").format(event.values[0])+" m/s²");
            infoTextViews[1].get(1).setText(new DecimalFormat("#.##").format(event.values[0])+" m/s²");
            infoTextViews[1].get(2).setText(new DecimalFormat("#.##").format(event.values[0])+" m/s²");
        }
        else if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE&&gyroscope!=null)
        {
            infoTextViews[3].get(0).setText(new DecimalFormat("#.##").format(event.values[0]) + " rad/s");
            infoTextViews[3].get(1).setText(new DecimalFormat("#.##").format(event.values[0]) + " rad/s");
            infoTextViews[3].get(2).setText(new DecimalFormat("#.##").format(event.values[0]) + " rad/s");
        }
        else if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD&&magnetic_field!=null)
        {
            infoTextViews[4].get(0).setText(new DecimalFormat("#.##").format(event.values[0]) + " μT");
            infoTextViews[4].get(1).setText(new DecimalFormat("#.##").format(event.values[0]) + " μT");
            infoTextViews[4].get(2).setText(new DecimalFormat("#.##").format(event.values[0]) + " μT");
        }
        else if(event.sensor.getType()==Sensor.TYPE_PRESSURE&&pressure!=null)
            infoTextViews[5].get(0).setText(new DecimalFormat("#.##").format(event.values[0])+" hPa");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(light!=null)
            sensorManager.registerListener(this,light,SensorManager.SENSOR_DELAY_NORMAL);
        if(accelerometer!=null)
            sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        if(gravity!=null)
            sensorManager.registerListener(this,gravity,SensorManager.SENSOR_DELAY_NORMAL);
        if(gyroscope!=null)
            sensorManager.registerListener(this,gyroscope,SensorManager.SENSOR_DELAY_NORMAL);
        if(magnetic_field!=null)
            sensorManager.registerListener(this,magnetic_field,SensorManager.SENSOR_DELAY_NORMAL);
        if(pressure!=null)
            sensorManager.registerListener(this,pressure,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(light!=null)
            sensorManager.unregisterListener(this,light);
        if(accelerometer!=null)
            sensorManager.unregisterListener(this,accelerometer);
        if(gravity!=null)
            sensorManager.unregisterListener(this,gravity);
        if(gyroscope!=null)
            sensorManager.unregisterListener(this,gyroscope);
        if(magnetic_field!=null)
            sensorManager.unregisterListener(this,magnetic_field);
        if(pressure!=null)
            sensorManager.unregisterListener(this,pressure);
    }
}
