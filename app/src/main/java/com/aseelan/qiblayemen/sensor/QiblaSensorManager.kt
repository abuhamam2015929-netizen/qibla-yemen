package com.aseelan.qiblayemen.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

data class CompassReading(
    val azimuthDegrees: Float,   // اتجاه شمال الجهاز الحقيقي (0-360)
    val accuracy: Int,           // دقة المستشعر: SensorManager.SENSOR_STATUS_*
    val magneticInterference: Boolean // true إذا كان المجال المغناطيسي خارج المدى الطبيعي (تشويش)
)

/**
 * يدمج بيانات المغناطيسية (Magnetometer) والتسارع (Accelerometer) لحساب اتجاه البوصلة
 * مع تعويض الميل (Tilt Compensation) - يعطي دقة أعلى بكثير من استخدام المغناطيسية وحدها،
 * خاصة عندما يكون الهاتف مائلاً وليس أفقياً تماماً.
 * يُطبَّق فلتر تنعيم أُسّي (Low-pass filter) لتفادي اهتزاز مؤشر البوصلة.
 */
class QiblaSensorManager(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var smoothedAzimuth: Float? = null
    private val filterAlpha = 0.15f // معامل الفلتر الأُسّي: كلما قلّ زادت النعومة

    // المدى الطبيعي لشدة المجال المغناطيسي الأرضي بوحدة µT (خارج هذا المدى = تشويش محتمل
    // من معدن قريب، مكبر صوت، شاحن، إلخ)
    private val normalMagneticFieldRange = 20f..80f

    fun hasRequiredSensors(): Boolean = accelerometer != null && magnetometer != null

    /**
     * تدفّق حي (Flow) لقراءات البوصلة المعالَجة، يُبث في كل تحديث للمستشعر.
     */
    fun compassFlow(): Flow<CompassReading> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        lowPass(event.values, gravity)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        lowPass(event.values, geomagnetic)
                    }
                }

                val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
                if (success) {
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    azimuth = (azimuth + 360) % 360

                    val smoothed = smoothAzimuth(azimuth)

                    val magnitude = sqrt(
                        geomagnetic[0] * geomagnetic[0] +
                        geomagnetic[1] * geomagnetic[1] +
                        geomagnetic[2] * geomagnetic[2]
                    )
                    val interference = magnitude !in normalMagneticFieldRange

                    trySend(CompassReading(smoothed, event.accuracy, interference))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // يمكن استخدامها لعرض تنبيه "يحتاج معايرة" عند SENSOR_STATUS_UNRELIABLE
            }
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = 0.85f // فلتر منفصل لتنعيم قراءات الحساسات الخام (تسارع/مغناطيسية)
        for (i in input.indices) {
            output[i] = alpha * output[i] + (1 - alpha) * input[i]
        }
    }

    /**
     * تنعيم زاوية الأزيموث مع معالجة صحيحة للالتفاف حول 0/360 درجة
     * (لتجنّب قفزة كاذبة عند الانتقال من 359 إلى 1 مثلاً).
     */
    private fun smoothAzimuth(newAzimuth: Float): Float {
        val prev = smoothedAzimuth
        if (prev == null) {
            smoothedAzimuth = newAzimuth
            return newAzimuth
        }
        var delta = newAzimuth - prev
        if (delta > 180) delta -= 360
        if (delta < -180) delta += 360
        val result = (prev + filterAlpha * delta + 360) % 360
        smoothedAzimuth = result
        return result
    }
}
