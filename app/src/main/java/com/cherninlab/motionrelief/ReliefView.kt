package com.cherninlab.motionrelief

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class ReliefView(context: Context) : View(context), SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    private var hasGravity = false
    private var hasMagnetic = false

    private var filteredRoll = 0f
    private var filteredPitch = 0f

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10141C")
        style = Paint.Style.FILL
    }
    private val seaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1D2430")
        style = Paint.Style.FILL
    }
    private val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 255, 255, 255)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 42f
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(210, 255, 255, 255)
        textSize = 28f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerSensors()
    }

    override fun onDetachedFromWindow() {
        sensorManager.unregisterListener(this)
        super.onDetachedFromWindow()
    }

    private fun registerSensors() {
        when {
            rotationSensor != null -> {
                sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
            }

            accelerometer != null && magnetometer != null -> {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                applyAngles(
                    rollRadians = -orientationAngles[2],
                    pitchRadians = orientationAngles[1]
                )
            }

            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, min(3, event.values.size))
                hasGravity = true
                updateFromLegacySensors()
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, min(3, event.values.size))
                hasMagnetic = true
                updateFromLegacySensors()
            }
        }
    }

    private fun updateFromLegacySensors() {
        if (!hasGravity || !hasMagnetic) return
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            applyAngles(
                rollRadians = -orientationAngles[2],
                pitchRadians = orientationAngles[1]
            )
        }
    }

    private fun applyAngles(rollRadians: Float, pitchRadians: Float) {
        val targetRoll = Math.toDegrees(rollRadians.toDouble()).toFloat().coerceIn(-45f, 45f)
        val targetPitch = Math.toDegrees(pitchRadians.toDouble()).toFloat().coerceIn(-25f, 25f)

        filteredRoll += (targetRoll - filteredRoll) * 0.14f
        filteredPitch += (targetPitch - filteredPitch) * 0.14f
        invalidate()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val pitchOffset = (filteredPitch / 25f) * (h * 0.18f)

        canvas.drawRect(0f, 0f, w, h, backgroundPaint)

        canvas.save()
        canvas.translate(cx, cy + pitchOffset)
        canvas.rotate(filteredRoll)

        val extent = w + h
        canvas.drawRect(-extent, -extent, extent, 0f, skyPaint)
        canvas.drawRect(-extent, 0f, extent, extent, seaPaint)
        canvas.drawLine(-extent, 0f, extent, 0f, horizonPaint)
        canvas.restore()

        val guideRadius = min(w, h) * 0.08f
        canvas.drawCircle(cx, cy, guideRadius, guidePaint)
        canvas.drawLine(cx - guideRadius * 1.8f, cy, cx - guideRadius * 0.65f, cy, guidePaint)
        canvas.drawLine(cx + guideRadius * 0.65f, cy, cx + guideRadius * 1.8f, cy, guidePaint)
        canvas.drawCircle(cx, cy, 8f, dotPaint)

        canvas.drawText(resources.getString(R.string.relief_title), 40f, 80f, textPaint)
        canvas.drawText(resources.getString(R.string.relief_hint), 40f, 124f, subTextPaint)

        if (rotationSensor == null && (accelerometer == null || magnetometer == null)) {
            canvas.drawText(resources.getString(R.string.sensor_unavailable), 40f, h - 60f, subTextPaint)
        }
    }
}
