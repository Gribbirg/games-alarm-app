package com.example.smartalarm.feature.games.maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.example.smartalarm.feature.games.maze.R

/**
 * Кастомная View, рисующая лабиринт в [onDraw]:
 * стены — линиями, игрок — кружком, выход — скруглённым квадратом.
 *
 * View только читает модель [Maze] и позицию игрока; сама модель про
 * Android не знает. Цвета берутся из атрибутов темы (colorOnBackground,
 * colorPrimary, colorTertiary) с контрастными значениями по умолчанию,
 * поэтому корректно выглядит и в светлой, и в тёмной теме.
 */
class MazeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var maze: Maze? = null
    private var playerRow = 0
    private var playerCol = 0

    private val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = themeColor(R.attr.colorOnBackground, Color.parseColor("#37474F"))
    }

    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = themeColor(R.attr.colorPrimary, Color.parseColor("#1E88E5"))
    }

    private val exitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = themeColor(R.attr.colorTertiary, Color.parseColor("#43A047"))
    }

    /** Задаёт лабиринт для отрисовки и перерисовывает View. */
    fun setMaze(maze: Maze) {
        this.maze = maze
        invalidate()
    }

    /** Обновляет позицию игрока (строка/столбец клетки) и перерисовывает View. */
    fun setPlayerPosition(row: Int, col: Int) {
        playerRow = row
        playerCol = col
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val maze = maze ?: return

        val strokePadding = 8f
        val availableWidth = width - paddingLeft - paddingRight - 2 * strokePadding
        val availableHeight = height - paddingTop - paddingBottom - 2 * strokePadding
        if (availableWidth <= 0 || availableHeight <= 0) return

        val cell = minOf(availableWidth / maze.cols, availableHeight / maze.rows)
        val offsetX = paddingLeft + strokePadding + (availableWidth - cell * maze.cols) / 2
        val offsetY = paddingTop + strokePadding + (availableHeight - cell * maze.rows) / 2

        wallPaint.strokeWidth = maxOf(3f, cell * 0.09f)

        drawExit(canvas, maze, cell, offsetX, offsetY)
        drawPlayer(canvas, cell, offsetX, offsetY)
        drawWalls(canvas, maze, cell, offsetX, offsetY)
    }

    private fun drawWalls(canvas: Canvas, maze: Maze, cell: Float, offsetX: Float, offsetY: Float) {
        for (row in 0 until maze.rows) {
            for (col in 0 until maze.cols) {
                val left = offsetX + col * cell
                val top = offsetY + row * cell
                if (maze.hasWall(row, col, Direction.UP))
                    canvas.drawLine(left, top, left + cell, top, wallPaint)
                if (maze.hasWall(row, col, Direction.LEFT))
                    canvas.drawLine(left, top, left, top + cell, wallPaint)
                // Нижняя и правая границы решётки рисуются один раз — у крайних клеток.
                if (row == maze.rows - 1 && maze.hasWall(row, col, Direction.DOWN))
                    canvas.drawLine(left, top + cell, left + cell, top + cell, wallPaint)
                if (col == maze.cols - 1 && maze.hasWall(row, col, Direction.RIGHT))
                    canvas.drawLine(left + cell, top, left + cell, top + cell, wallPaint)
            }
        }
    }

    private fun drawExit(canvas: Canvas, maze: Maze, cell: Float, offsetX: Float, offsetY: Float) {
        val inset = cell * 0.22f
        val left = offsetX + (maze.cols - 1) * cell + inset
        val top = offsetY + (maze.rows - 1) * cell + inset
        val size = cell - 2 * inset
        canvas.drawRoundRect(left, top, left + size, top + size, size * 0.25f, size * 0.25f, exitPaint)
    }

    private fun drawPlayer(canvas: Canvas, cell: Float, offsetX: Float, offsetY: Float) {
        val cx = offsetX + playerCol * cell + cell / 2
        val cy = offsetY + playerRow * cell + cell / 2
        canvas.drawCircle(cx, cy, cell * 0.3f, playerPaint)
    }

    /**
     * Разрешает цветовой атрибут темы; при неудаче возвращает [fallback],
     * чтобы лабиринт оставался читаемым в любой теме.
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
