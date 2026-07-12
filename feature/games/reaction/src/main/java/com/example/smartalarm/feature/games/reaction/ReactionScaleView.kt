package com.example.smartalarm.feature.games.reaction

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.example.smartalarm.feature.games.reaction.R

/**
 * Кастомная View-шкала игры «Поймай момент»: дорожка на всю ширину,
 * подсвеченная целевая зона и вертикальный бегунок.
 *
 * View только рисует состояние, которое ей задают снаружи
 * ([setZone], [setIndicatorPosition]); сама логика движения и попаданий
 * про Android не знает. Цвета берутся из атрибутов темы
 * (colorSurfaceVariant, colorTertiary, colorOnBackground) с контрастными
 * значениями по умолчанию, поэтому шкала корректно выглядит и в светлой,
 * и в тёмной теме.
 */
class ReactionScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var indicatorPosition = 0f
    private var zoneStart = 0f
    private var zoneEnd = 0f

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = themeColor(R.attr.colorSurfaceVariant, Color.parseColor("#CFD8DC"))
    }

    private val zonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = themeColor(R.attr.colorTertiary, Color.parseColor("#43A047"))
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = themeColor(R.attr.colorOnBackground, Color.parseColor("#37474F"))
    }

    /**
     * Задаёт целевую зону и перерисовывает шкалу.
     *
     * @param start левая граница зоны, 0.0..1.0
     * @param end правая граница зоны, 0.0..1.0
     */
    fun setZone(start: Float, end: Float) {
        zoneStart = start.coerceIn(0f, 1f)
        zoneEnd = end.coerceIn(0f, 1f)
        invalidate()
    }

    /**
     * Задаёт позицию бегунка и перерисовывает шкалу.
     *
     * @param position позиция на шкале, 0.0..1.0
     */
    fun setIndicatorPosition(position: Float) {
        indicatorPosition = position.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        if (contentWidth <= 0 || contentHeight <= 0) return

        val indicatorWidth = maxOf(6f, contentWidth * 0.012f)
        // Дорожка чуть уже контента, чтобы бегунок на краях не обрезался.
        val trackLeft = paddingLeft + indicatorWidth / 2
        val trackRight = paddingLeft + contentWidth - indicatorWidth / 2
        val trackWidth = trackRight - trackLeft

        val trackHeight = contentHeight * 0.5f
        val trackTop = paddingTop + (contentHeight - trackHeight) / 2
        val trackBottom = trackTop + trackHeight
        val cornerRadius = trackHeight / 2

        canvas.drawRoundRect(
            trackLeft, trackTop, trackRight, trackBottom,
            cornerRadius, cornerRadius, trackPaint
        )

        if (zoneEnd > zoneStart) {
            canvas.drawRoundRect(
                trackLeft + trackWidth * zoneStart, trackTop,
                trackLeft + trackWidth * zoneEnd, trackBottom,
                cornerRadius, cornerRadius, zonePaint
            )
        }

        val indicatorCenter = trackLeft + trackWidth * indicatorPosition
        canvas.drawRoundRect(
            indicatorCenter - indicatorWidth / 2, paddingTop.toFloat(),
            indicatorCenter + indicatorWidth / 2, (paddingTop + contentHeight).toFloat(),
            indicatorWidth / 2, indicatorWidth / 2, indicatorPaint
        )
    }

    /**
     * Разрешает цветовой атрибут темы; при неудаче возвращает [fallback],
     * чтобы шкала оставалась читаемой в любой теме.
     */
    private fun themeColor(attrId: Int, fallback: Int): Int {
        val value = TypedValue()
        if (!context.theme.resolveAttribute(attrId, value, true)) return fallback
        return when {
            value.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                    value.type <= TypedValue.TYPE_LAST_COLOR_INT -> value.data

            value.resourceId != 0 -> ContextCompat.getColor(context, value.resourceId)
            else -> fallback
        }
    }
}
