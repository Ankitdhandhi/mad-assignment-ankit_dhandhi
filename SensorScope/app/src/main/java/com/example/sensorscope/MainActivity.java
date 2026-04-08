package com.example.sensorscope;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelSensor, lightSensor, proximitySensor;

    private TextView tvAccelX, tvAccelY, tvAccelZ;
    private TextView tvLight, tvLightLabel;
    private TextView tvProximity, tvProximityLabel;
    private ProgressBar pbAccel, pbLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        tvAccelX = findViewById(R.id.tvAccelX);
        tvAccelY = findViewById(R.id.tvAccelY);
        tvAccelZ = findViewById(R.id.tvAccelZ);
        tvLight = findViewById(R.id.tvLight);
        tvLightLabel = findViewById(R.id.tvLightLabel);
        tvProximity = findViewById(R.id.tvProximity);
        tvProximityLabel = findViewById(R.id.tvProximityLabel);
        pbAccel = findViewById(R.id.pbAccel);
        pbLight = findViewById(R.id.pbLight);

        // Prevent crash if XML missing something
        if (tvAccelX == null || pbAccel == null) {
            return; // avoid crash
        }

        // Sensor Manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        // Sensor availability check
        if (accelSensor == null) tvAccelX.setText("Accelerometer not available");
        if (lightSensor == null) tvLight.setText("Light sensor not available");
        if (proximitySensor == null) tvProximity.setText("Proximity sensor not available");

        // Set ProgressBar max (IMPORTANT)
        if (pbAccel != null) pbAccel.setMax(300);
        if (pbLight != null) pbLight.setMax(10000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sensorManager != null) {
            if (accelSensor != null)
                sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);

            if (lightSensor != null)
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);

            if (proximitySensor != null)
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event == null || event.sensor == null) return;

        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                tvAccelX.setText("X: " + String.format("%.3f", x) + " m/s²");
                tvAccelY.setText("Y: " + String.format("%.3f", y) + " m/s²");
                tvAccelZ.setText("Z: " + String.format("%.3f", z) + " m/s²");

                double magnitude = Math.sqrt(x * x + y * y + z * z);
                if (pbAccel != null)
                    pbAccel.setProgress((int) Math.min(Math.max(magnitude * 10, 0), 300));
                break;

            case Sensor.TYPE_LIGHT:
                float lux = event.values[0];

                tvLight.setText("Lux: " + String.format("%.1f", lux));

                if (pbLight != null)
                    pbLight.setProgress((int) Math.min(Math.max(lux, 0), 10000));

                String condition;
                if (lux < 10) condition = "Dark 🌑";
                else if (lux < 100) condition = "Dim 🌘";
                else if (lux < 1000) condition = "Indoor 💡";
                else if (lux < 5000) condition = "Bright ☀️";
                else condition = "Direct Sunlight 🌞";

                tvLightLabel.setText("Condition: " + condition);
                break;

            case Sensor.TYPE_PROXIMITY:
                float dist = event.values[0];

                tvProximity.setText("Distance: " + String.format("%.1f", dist) + " cm");

                String status = (dist < 5f) ? "Object nearby 🟡" : "Clear ✅";
                tvProximityLabel.setText("Status: " + status);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}