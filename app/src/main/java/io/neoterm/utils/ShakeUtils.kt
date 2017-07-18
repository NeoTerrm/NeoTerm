package io.neoterm.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeUtils(context: Context) : SensorEventListener {
    companion object {
        private val SHAKE_SENSITIVITY = 14
    }

    private val mSensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var mOnShakeListener: OnShakeListener? = null

    fun setOnShakeListener(onShakeListener: OnShakeListener) {
        mOnShakeListener = onShakeListener
    }

    fun onResume() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun onPause() {
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorType = event.sensor.type
        //values[0]:X, values[1]：Y, values[2]：Z
        val values = event.values
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            //这里可以调节摇一摇的灵敏度
            if (Math.abs(values[0]) > SHAKE_SENSITIVITY || Math.abs(values[1]) > SHAKE_SENSITIVITY || Math.abs(values[2]) > SHAKE_SENSITIVITY) {
                if (mOnShakeListener != null) {
                    mOnShakeListener!!.onShake()
                }
            }
        }
    }

    interface OnShakeListener {
        fun onShake()
    }
}