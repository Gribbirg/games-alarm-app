package com.example.smartalarm.feature.games.hanoi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.smartalarm.feature.games.hanoi.R

/**
 * Кастомная View игры «Ханойская башня»: рисует в [onDraw] основание,
 * три стержня и диски скруглёнными прямоугольниками.
 *
 * View только отображает снимок состояния [HanoiGameLogic.rodsSnapshot]
 * и ничего не знает о правилах: тап по любой из трёх вертикальных третей
 * прокидывается наружу через [onRodTapped], а решение о ходе принимает
 * фрагмент/логика. Верхний диск выбранного стержня рисуется «поднятым»
 * над стержнем и акцентным цветом.
 *
 * Цвета берутся из атрибутов темы (colorPrimary, colorTertiary,
 * colorOnBackground) с контрастными значениями по умолчанию, поэтому
 * View корректно выглядит и в светлой, и в тёмной теме.
 */
class HanoiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Колбэк тапа по стержню: аргумент — индекс стержня 0..2. */
    var onRodTapped: ((Int) -> Unit)? = null

    private var rods: List<List<Int>> = listOf(emptyList(), emptyList(), emptyList())
    private var diskCount = 3
    private var selectedRod: Int? = null
    private var tappedRod: Int? = null

    private val diskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = themeColor(R.attr.colorPrimary, Color.parseColor("#1E88E5"))
    }

    private val selectedDiskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = themeColor(R.attr.colorTertiary, Color.parseColor("#FB8C00"))
    }

    private val rodPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = themeColor(R.attr.colorOnBackground, Color.parseColor("#37474F"))
    }

    /**
     * Обновляет отображаемое состояние и перерисовывает View.
     *
     * @param rods снимок стержней (списки размеров дисков снизу вверх)
     * @param diskCount общее число дисков (задаёт масштаб отрисовки)
     * @param selectedRod индекс выбранного стержня, чей верхний диск
     * нужно подсветить, или `null`, если ничего не выбрано
     */
    fun setState(rods: List<List<Int>>, diskCount: Int, selectedRod: Int?) {
        this.rods = rods
        this.diskCount = diskCount
        this.selectedRod = selectedRod
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val availableWidth = (width - paddingLeft - paddingRight).toFloat()
        val availableHeight = (height - paddingTop - paddingBottom).toFloat()
        if (availableWidth <= 0 || availableHeight <= 0) return

        val columnWidth = availableWidth / 3f
        // Высота диска: башня + место для «поднятого» диска и основание.
        val diskHeight = availableHeight / (diskCount + 4)
        val baseTop = paddingTop + availableHeight - diskHeight * 0.5f
        val rodHeight = diskHeight * (diskCount + 1)
        val rodWidth = maxOf(6f, diskHeight * 0.22f)

        // Основание.
        canvas.drawRoundRect(
            paddingLeft + columnWidth * 0.05f, baseTop,
            paddingLeft + availableWidth - columnWidth * 0.05f, baseTop + diskHeight * 0.4f,
            diskHeight * 0.2f, diskHeight * 0.2f, rodPaint
        )

        for (rod in 0..2) {
            val centerX = paddingLeft + columnWidth * (rod + 0.5f)

            // Стержень.
            canvas.drawRoundRect(
                centerX - rodWidth / 2, baseTop - rodHeight,
                centerX + rodWidth / 2, baseTop,
                rodWidth / 2, rodWidth / 2, rodPaint
            )

            val disks = rods[rod]
            val liftedIndex = if (rod == selectedRod) disks.size - 1 else -1
            disks.forEachIndexed { index, size ->
                if (index == liftedIndex) {
                    // Выбранный (верхний) диск — «поднят» над стержнем.
                    drawDisk(
                        canvas, centerX, baseTop - rodHeight - diskHeight * 0.4f,
                        size, columnWidth, diskHeight, selectedDiskPaint
                    )
                } else {
                    drawDisk(
                        canvas, centerX, baseTop - index * diskHeight,
                        size, columnWidth, diskHeight, diskPaint
                    )
                }
            }
        }
    }

    /**
     * Рисует один диск размера [size], нижняя грань которого лежит
     * на высоте [bottom], по центру [centerX].
     */
    private fun drawDisk(
        canvas: Canvas,
        centerX: Float,
        bottom: Float,
        size: Int,
        columnWidth: Float,
        diskHeight: Float,
        paint: Paint
    ) {
        val diskWidth = columnWidth * (0.32f + 0.58f * size / diskCount)
        val inset = diskHeight * 0.08f
        canvas.drawRoundRect(
            centerX - diskWidth / 2, bottom - diskHeight + inset,
            centerX + diskWidth / 2, bottom - inset,
            diskHeight * 0.4f, diskHeight * 0.4f, paint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (width > 0) {
                    tappedRod = (event.x / (width / 3f)).toInt().coerceIn(0, 2)
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        tappedRod?.let { onRodTapped?.invoke(it) }
        tappedRod = null
        return true
    }

    /**
     * Разрешает цветовой атрибут темы; при неудаче возвращает [fallback],
     * чтобы башня оставалась читаемой в любой теме.
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
