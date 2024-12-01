package com.example.test

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0f // Progress percentage (0 to 100)
    private val circleBounds = RectF()

    // Background circle paint
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray) // Background color
        style = Paint.Style.STROKE
        strokeWidth = 20f
    }

    // Progress arc paint
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.holo_green_light) // Progress color
        style = Paint.Style.STROKE
        strokeWidth = 20f
    }

    // Percentage text paint
    private val percentagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.white) // Text color
        textSize = 62f
        textAlign = Paint.Align.CENTER
    }

    // "Confidence" label paint
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.white) // Text color
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }

    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 100f)
        invalidate() // Redraw the view with updated progress
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calculate bounds for the circle
        val size = Math.min(width, height).toFloat()
        val padding = backgroundPaint.strokeWidth / 2 // Ensure padding matches the stroke width
        circleBounds.set(padding, padding, size - padding, size - padding)

        // Draw background circle
        canvas.drawArc(circleBounds, 0f, 360f, false, backgroundPaint)

        // Draw progress arc
        canvas.drawArc(circleBounds, -90f, (progress / 100) * 360f, false, progressPaint)

        // Draw "Confidence" label above percentage
        canvas.drawText(
            "Confidence",
            width / 2f,
            height / 2f - 40f, // Position above the percentage text
            labelPaint
        )

        // Draw percentage text in the center
        canvas.drawText(
            "${progress.toInt()}%",
            width / 2f,
            height / 2f + 40f, // Center percentage text
            percentagePaint
        )
    }
}
