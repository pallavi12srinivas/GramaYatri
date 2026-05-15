package com.gramayatri.app.ui.routes

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.gramayatri.app.data.BusPing
import com.gramayatri.app.data.BusRoute

class TrackingTimelineView(context: Context) : View(context) {
    private var route: BusRoute? = null
    private var latestPing: BusPing? = null
    private var cancelled: Boolean = false

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(205, 218, 205)
        strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(46, 125, 50)
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(31, 48, 38)
        textSize = 38f
    }
    private val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(88, 105, 94)
        textSize = 30f
    }
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(46, 125, 50)
        textSize = 31f
        isFakeBoldText = true
    }
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(231, 243, 231)
    }
    private val measureRect = Rect()

    fun setTracking(route: BusRoute, latestPing: BusPing?, cancelled: Boolean) {
        this.route = route
        this.latestPing = latestPing
        this.cancelled = cancelled
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val stopCount = route?.stops?.size ?: 1
        val desiredHeight = paddingTop + paddingBottom + 96 + (stopCount.coerceAtLeast(2) - 1) * 118
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val route = route ?: return
        if (route.stops.isEmpty()) return

        val latestIndex = latestPing?.stopIndex ?: 0
        val startY = paddingTop + 54f
        val gap = if (route.stops.size == 1) 0f else (height - paddingTop - paddingBottom - 108f) / (route.stops.size - 1)
        val x = paddingLeft + 36f
        val textX = x + 56f
        val endY = startY + gap * (route.stops.size - 1)

        canvas.drawLine(x, startY, x, endY, linePaint)
        if (!cancelled && latestIndex > 0) {
            canvas.drawLine(x, startY, x, startY + gap * latestIndex.coerceAtMost(route.stops.lastIndex), progressPaint)
        }

        route.stops.forEachIndexed { index, stop ->
            val y = startY + gap * index
            val isDone = index < latestIndex && !cancelled
            val isActive = index == latestIndex && !cancelled
            dotPaint.color = when {
                cancelled -> Color.rgb(176, 82, 38)
                isActive -> Color.rgb(22, 101, 52)
                isDone -> Color.rgb(46, 125, 50)
                else -> Color.WHITE
            }
            dotPaint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, if (isActive) 18f else 15f, dotPaint)
            dotPaint.style = Paint.Style.STROKE
            dotPaint.strokeWidth = 5f
            dotPaint.color = if (isActive || isDone) Color.rgb(46, 125, 50) else Color.rgb(142, 157, 145)
            canvas.drawCircle(x, y, if (isActive) 18f else 15f, dotPaint)

            canvas.drawText(stop.name, textX, y - 8f, textPaint)
            val timeText = if (index == 0) "Start stop" else "+${stop.averageMinutesFromPrevious} min from previous"
            canvas.drawText(timeText, textX, y + 31f, metaPaint)

            if (isActive) drawActiveLabel(canvas, textX, y + 68f)
        }
    }

    private fun drawActiveLabel(canvas: Canvas, left: Float, baseline: Float) {
        val ping = latestPing ?: return
        val label = "Tracking: ${ping.reporterName}"
        activePaint.getTextBounds(label, 0, label.length, measureRect)
        val bubbleRight = (left + measureRect.width() + 32f).coerceAtMost(width - paddingRight.toFloat())
        canvas.drawRoundRect(left - 12f, baseline - 36f, bubbleRight, baseline + 13f, 16f, 16f, bubblePaint)
        canvas.drawText(label, left, baseline, activePaint)
    }
}