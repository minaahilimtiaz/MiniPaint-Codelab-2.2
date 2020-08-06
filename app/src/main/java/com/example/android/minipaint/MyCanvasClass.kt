package com.example.android.minipaint

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat

class MyCanvasClass (context : Context) : View(context){
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    //rectnagle for drawing frame on canvas
    private lateinit var frame: Rect
    //ResourcesCompat helps in accessing features from resources
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private val STROKE_WIDTH = 12f // has to be float
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)
    //to store path of what user is drawing
    private var path = Path()
    //for caching the coordinates of touch event
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    //for storing latest values
    private var currentX = 0f
    private var currentY = 0f
    //for measuring if there has been an actual movement
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    private val paint = Paint()

    init{
        setPaintProperties(paint)
    }

    private fun setPaintProperties(paint: Paint) {
        paint.apply {
            color = drawColor
            // Smooths out edges of what is drawn without affecting shape.
            isAntiAlias = true
            // Dithering affects how colors with higher-precision than the device are down-sampled.
            isDither = true
            style = Paint.Style.STROKE // default: FILL
            strokeJoin = Paint.Join.ROUND // styling the point where lines meet
            strokeCap = Paint.Cap.ROUND // default: BUTT
            strokeWidth = STROKE_WIDTH //width of stroke
        }
    }


    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        setFrameDimension()
        if (::extraBitmap.isInitialized)
            extraBitmap.recycle()
        //for storing colour in 4 bytes following bitmap configuration
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
        canvas.drawRect(frame, paint)
    }

    private fun setFrameDimension() {
        val inset = 40
        frame = Rect(100, 130, width - inset, height - inset)
    }

    //this function captures the user's movement related data
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //input of user movement point on screen
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun touchStart() {
        //clear previous path so it is not be re-drawn
        path.reset()
        //set the beginning point of path
        path.moveTo(motionTouchEventX, motionTouchEventY)
        updateCoordinates()
    }

    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        //to detect if there has been an actual movement
        if (dx >= touchTolerance || dy >= touchTolerance) {
            //draw the curve
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            updateCoordinates()
            // Cache the path in the extra bitmap
            extraCanvas.drawPath(path, paint)
        }
        invalidate()
    }

    private fun updateCoordinates() {
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset()
    }


}