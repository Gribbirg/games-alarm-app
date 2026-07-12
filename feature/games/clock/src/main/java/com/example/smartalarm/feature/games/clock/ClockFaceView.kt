package com.example.smartalarm.feature.games.clock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Кастомная View аналогового циферблата для игры «Который час».
 *
 * Рисует круглый циферблат с минутными и часовыми отметками, цифрами 1..12,
 * часовой и минутной стрелками. View сама углы не считает — готовые значения
 * в градусах задаются через [setHandAngles] (их считает [ClockTime]).
 *
 * Цвета берутся из атрибутов темы (colorPrimary — часовая стрелка,
 * colorSecondary — минутная, colorOnSurface — циферблат и цифры)
 * с фолбэками на константы, поэтому циферблат корректно выглядит
 * и в светлой, и в тёмной темах.
 */
class ClockFaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var hourHandAngle = 0f
    private var minuteHandAngle = 0f

    private val density = resources.displayMetrics.density

    private val dialColor =
        resolveThemeColor(com.google.android.material.R.attr.colorOnSurface, Color.DKGRAY)
    private val hourHandColor =
        resolveThemeColor(com.google.android.material.R.attr.colorPrimary, Color.BLACK)
    private val minuteHandColor =
        resolveThemeColor(com.google.android.material.R.attr.colorSecondary, Color.GRAY)

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
        color = dialColor
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = dialColor
    }

    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        color = dialColor
    }

    private val hourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 7f * density
        color = hourHandColor
    }

    private val minuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 4f * density
        color = minuteHandColor
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = hourHandColor
    }

    /**
     * Задаёт углы стрелок в градусах по часовой стрелке от отметки «12»
     * и перерисовывает циферблат.
     *
     * @param hourDegrees угол часовой стрелки (см. [ClockTime.hourHandAngle])
     * @param minuteDegrees угол минутной стрелки (см. [ClockTime.minuteHandAngle])
     */
    fun setHandAngles(hourDegrees: Float, minuteDegrees: Float) {
        hourHandAngle = hourDegrees
        minuteHandAngle = minuteDegrees
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 2f - 4f * density
        if (radius <= 0f) return

        canvas.drawCircle(centerX, centerY, radius, ringPaint)
        drawTicks(canvas, centerX, centerY, radius)
        drawNumbers(canvas, centerX, centerY, radius)
        drawHand(canvas, centerX, centerY, hourHandAngle, radius * HOUR_HAND_LENGTH, hourHandPaint)
        drawHand(
            canvas, centerX, centerY,
            minuteHandAngle, radius * MINUTE_HAND_LENGTH, minuteHandPaint
        )
        canvas.drawCircle(centerX, centerY, 4f * density, centerPaint)
    }

    private fun drawTicks(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        for (minute in 0 until 60) {
            val isHourTick = minute % 5 == 0
            tickPaint.strokeWidth = (if (isHourTick) 2.5f else 1f) * density
            val innerRadius = radius * if (isHourTick) 0.90f else 0.95f
            val angle = Math.toRadians(minute * 6.0)
            val sin = sin(angle).toFloat()
            val cos = cos(angle).toFloat()
            canvas.drawLine(
                centerX + innerRadius * sin, centerY - innerRadius * cos,
                centerX + radius * sin, centerY - radius * cos,
                tickPaint
            )
        }
    }

    private fun drawNumbers(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        numberPaint.textSize = radius * NUMBER_TEXT_SIZE
        val numberRadius = radius * NUMBER_RADIUS
        for (hour in 1..12) {
            val angle = Math.toRadians(hour * 30.0)
            val x = centerX + numberRadius * sin(angle).toFloat()
            val y = centerY - numberRadius * cos(angle).toFloat() -
                    (numberPaint.ascent() + numberPaint.descent()) / 2f
            canvas.drawText(hour.toString(), x, y, numberPaint)
        }
    }

    private fun drawHand(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        angleDegrees: Float,
        length: Float,
        paint: Paint
    ) {
        val angle = Math.toRadians(angleDegrees.toDouble())
        canvas.drawLine(
            centerX, centerY,
            centerX + length * sin(angle).toFloat(),
            centerY - length * cos(angle).toFloat(),
            paint
        )
    }

    private fun resolveThemeColor(attr: Int, fallback: Int): Int {
        val value = TypedValue()
        if (!context.theme.resolveAttribute(attr, value, true)) return fallback
        return when {
            value.resourceId != 0 -> ContextCompat.getColor(context, value.resourceId)
            value.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT ->
                value.data

            else -> fallback
        }
    }

    private companion object {
        /** Длина часовой стрелки в долях радиуса. */
        const val HOUR_HAND_LENGTH = 0.52f

        /** Длина минутной стрелки в долях радиуса. */
        const val MINUTE_HAND_LENGTH = 0.78f

        /** Размер цифр в долях радиуса. */
        const val NUMBER_TEXT_SIZE = 0.15f

        /** Радиус окружности, по которой расставлены цифры, в долях радиуса. */
        const val NUMBER_RADIUS = 0.75f
    }
}
