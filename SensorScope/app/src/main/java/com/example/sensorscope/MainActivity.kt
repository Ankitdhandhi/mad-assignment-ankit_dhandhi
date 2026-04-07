package com.example.sensorscope

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null

    private lateinit var tvAccelX: TextView
    private lateinit var tvAccelY: TextView
    private lateinit var tvAccelZ: TextView
    private lateinit var tvLight: TextView
    private lateinit var tvLightLabel: TextView
    private lateinit var tvProximity: TextView
    private lateinit var tvProximityLabel: TextView
    private lateinit var pbAccel: ProgressBar
    private lateinit var pbLight: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAccelX = findViewById(R.id.tvAccelX)
        tvAccelY = findViewById(R.id.tvAccelY)
        tvAccelZ = findViewById(R.id.tvAccelZ)
        tvLight = findViewById(R.id.tvLight)
        tvLightLabel = findViewById(R.id.tvLightLabel)
        tvProximity = findViewById(R.id.tvProximity)
        tvProximityLabel = findViewById(R.id.tvProximityLabel)
        pbAccel = findViewById(R.id.pbAccel)
        pbLight = findViewById(R.id.pbLight)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (accelSensor == null) tvAccelX.text = "Accelerometer not available"
        if (lightSensor == null) tvLight.text = "Light sensor not available"
        if (proximitySensor == null) tvProximity.text = "Proximity sensor not available"
    }

    override fun onResume() {
        super.onResume()
        accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        proximitySensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                tvAccelX.text = "X: ${"%.3f".format(x)} m/s²"
                tvAccelY.text = "Y: ${"%.3f".format(y)} m/s²"
                tvAccelZ.text = "Z: ${"%.3f".format(z)} m/s²"
                val magnitude = sqrt(x * x + y * y + z * z)
                pbAccel.progress = (magnitude * 10).toInt().coerceIn(0, 300)
            }
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                tvLight.text = "Lux: ${"%.1f".format(lux)}"
                pbLight.progress = lux.toInt().coerceIn(0, 10000)
                tvLightLabel.text = "Condition: " + when {
                    lux < 10 -> "Dark 🌑"
                    lux < 100 -> "Dim 🌘"
                    lux < 1000 -> "Indoor 💡"
                    lux < 5000 -> "Bright ☀️"
                    else -> "Direct Sunlight 🌞"
                }
            }
            Sensor.TYPE_PROXIMITY -> {
                val dist = event.values[0]
                tvProximity.text = "Distance: ${"%.1f".format(dist)} cm"
                tvProximityLabel.text = "Status: " + if (dist < 5f) "Object nearby 🟡" else "Clear ✅"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}