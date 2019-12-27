package com.osshare.image.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import androidx.appcompat.widget.AppCompatImageView
import com.osshare.image.R
import com.osshare.image.annotation.ViewShape
import java.io.File
import java.io.FileOutputStream


class ImageCropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), ViewTreeObserver.OnGlobalLayoutListener,
    ScaleGestureDetector.OnScaleGestureListener {

    @ViewShape
    var viewShape = ViewShape.CIRCLE
    private var viewStrokeWidth: Float = 1f
    private var viewStrokeColor: Int = Color.WHITE
    private var viewMaskColor: Int = Color.parseColor("#77000000")
    var aspectX: Int = 1
    var aspectY: Int = 1
    private val touchSlop: Int

    var overViewOffset = RectF()
    private var overViewSpace: Int = 0
    private var imageSpace: Int = 0

    private val paint: Paint
    private val paintS: Paint

    private var gestureDetector: GestureDetector
    private var scaleGestureDetector: ScaleGestureDetector

    var mValues = FloatArray(9)
    var mMatrix = Matrix()
    var initScale: Float = 0f
    var minScale: Float = 0f
    var maxScale: Float = 0f
    var max = 6

    var overViewPath = Path()
    var overView = RectF()


    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ImageCropView)
            viewStrokeWidth =
                a.getDimension(R.styleable.ImageCropView_viewStrokeWidth, viewStrokeWidth)
            viewStrokeColor =
                a.getColor(R.styleable.ImageCropView_viewStrokeColor, viewStrokeColor)
            viewMaskColor =
                a.getColor(R.styleable.ImageCropView_viewStrokeColor, viewMaskColor)
            aspectX = a.getInt(R.styleable.ImageCropView_aspectX, aspectX)
            aspectY = a.getInt(R.styleable.ImageCropView_aspectY, aspectY)
            viewShape = if (aspectX != aspectY) ViewShape.SQUARE else viewShape
            a.recycle()
        }
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop


        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        scaleType = ScaleType.MATRIX

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = viewMaskColor
        paintS = Paint(Paint.ANTI_ALIAS_FLAG)
        paintS.color = viewStrokeColor
        paintS.style = Paint.Style.STROKE
        paintS.strokeWidth = viewStrokeWidth

        if (overViewSpace == 0) overViewSpace =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42f, resources.displayMetrics)
                .toInt()

        imageSpace =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)
                .toInt()

        val gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val currScale: Float = getCurrentScale()
                val midScale: Float = maxScale / (max / 2f)
                if (currScale < midScale) {
                    val rScale = midScale / currScale
                    scale(rScale, rScale, e.x, e.y)
                } else {
                    val rScale: Float = initScale / currScale
                    scale(rScale, rScale, e.x, e.y)
                }
                return true
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val dx: Float
                val dy: Float
                val matrixRectF: RectF = getMatrixRectF()
                if (distanceX < 0) {
                    dx = distanceX.coerceAtLeast(matrixRectF.left - overViewOffset.left)
                } else {
                    dx = distanceX.coerceAtMost(matrixRectF.right - overViewOffset.right)
                }
                if (distanceY < 0) {
                    dy = distanceY.coerceAtLeast(matrixRectF.top - overViewOffset.top)
                } else {
                    dy = distanceY.coerceAtMost(matrixRectF.bottom - overViewOffset.bottom)
                }
                translate(-dx, -dy)
                return true
            }
        }

        gestureDetector = GestureDetector(context, gestureListener)
        scaleGestureDetector = ScaleGestureDetector(context, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        val drawable = drawable ?: return

        val scale: Float
        val viewWidth = width
        val viewHeight = height
        val overViewSize: Int = Math.min(viewWidth, viewHeight) - overViewSpace * 2
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        //build overview path
        //build overview path
        overViewPath.addRect(
            0f,
            0f,
            viewWidth.toFloat(),
            viewHeight.toFloat(),
            Path.Direction.CCW
        )
        val circlePath = Path()
        val radius = overViewSize / 2.toFloat()
        if (viewShape == ViewShape.CIRCLE) {
            circlePath.addCircle(
                viewWidth / 2.toFloat(),
                viewHeight / 2.toFloat(),
                radius,
                Path.Direction.CCW
            )
        } else {
            circlePath.addRect(
                viewWidth / 2f - radius,
                viewHeight / 2f - radius,
                viewWidth / 2f + radius,
                viewHeight / 2f + radius,
                Path.Direction.CCW
            )
        }
        overViewPath.op(circlePath, Path.Op.XOR)

        overView.set(
            viewWidth / 2f - radius, viewHeight / 2f - radius,
            viewWidth / 2f + radius, viewHeight / 2f + radius
        )
        overViewOffset.set(overView)
        overViewOffset.inset(-imageSpace.toFloat(), -imageSpace.toFloat())
        //calculate the scale
        //calculate the scale
        scale = (overViewSize + imageSpace * 2) * 1f / Math.min(
            drawableWidth,
            drawableHeight
        )
        initScale = scale
        minScale = initScale
        maxScale = minScale * max
        //calculate the delta move to center
        //calculate the delta move to center
        val dx = viewWidth / 2 - drawableWidth / 2
        val dy = viewHeight / 2 - drawableHeight / 2

        mMatrix.postTranslate(dx.toFloat(), dy.toFloat())
        mMatrix.postScale(initScale, initScale, viewWidth / 2.toFloat(), viewHeight / 2.toFloat())
        imageMatrix = mMatrix

        viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val gesture1: Boolean = gestureDetector.onTouchEvent(event)
        val gesture2: Boolean = scaleGestureDetector.onTouchEvent(event)
        return if (gesture1 || gesture2) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawPath(overViewPath, paint)

        if (viewStrokeWidth > 0) {
            canvas.drawCircle(
                overView.centerX(), overView.centerY()
                , (overView.width() + viewStrokeWidth) / 2, paintS
            )
        }
    }


    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return drawable != null
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val currScale = getCurrentScale()
        var scaleFactor = detector!!.scaleFactor

        scaleFactor = if (scaleFactor < 1 && currScale > minScale) {
            Math.max(scaleFactor, minScale / currScale)
        } else if (scaleFactor > 1 && currScale < maxScale) {
            Math.min(scaleFactor, maxScale / currScale)
        } else {
            1f
        }
        if (scaleFactor != 1f) {
            scale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
        }
        return true
    }


    fun getMatrixRectF(): RectF {
        val drawable = drawable ?: return RectF()
        val matrix = mMatrix
        val rect = RectF()
        rect.set(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        matrix.mapRect(rect)
        return rect
    }

    fun getCurrentScale(): Float {
        mMatrix.getValues(mValues)
        return mValues[Matrix.MSCALE_X]
    }

    fun scale(sx: Float, sy: Float, px: Float, py: Float) {
        mMatrix.postScale(sx, sy, px, py)
        checkMatrixBounds()
        imageMatrix = mMatrix
    }

    fun translate(dx: Float, dy: Float) {
        mMatrix.postTranslate(dx, dy)
        checkMatrixBounds()
        imageMatrix = mMatrix
    }

    private fun checkMatrixBounds() {
        val matrixRectF = getMatrixRectF()
        var dx = 0f
        var dy = 0f
        if (matrixRectF.left > overViewOffset.left) {
            dx = overViewOffset.left - matrixRectF.left
        } else if (matrixRectF.right < overViewOffset.right) {
            dx = overViewOffset.right - matrixRectF.right
        }
        if (matrixRectF.top > overViewOffset.top) {
            dy = overViewOffset.top - matrixRectF.top
        } else if (matrixRectF.bottom < overViewOffset.bottom) {
            dy = overViewOffset.bottom - matrixRectF.bottom
        }
        if (dx != 0f || dy != 0f) {
            mMatrix.postTranslate(dx, dy)
        }
    }


    fun crop(file: File, outputX: Int, outputY: Int): Boolean {
        val bitmap = cropImage(outputX, outputY) ?: return false
        try {
            if (!file.exists()) {
                if (!file.parentFile.mkdirs() || !file.createNewFile()) {
                    return false
                }
            }
            val fos = FileOutputStream(file)

            val thread = Thread(Runnable {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            })
            thread.join()
            fos.flush()
            fos.close()
            bitmap.recycle()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    fun cropImage(outputX: Int, outputY: Int): Bitmap? {
        val drawable = (drawable ?: return null) as? BitmapDrawable ?: return null
        val scale = getCurrentScale()
        val mRectF = getMatrixRectF()
        val w: Float = overView.width() / scale
        val h: Float = overView.height() / scale
        val left: Float = (overView.left - mRectF.left) / scale
        val top: Float = (overView.top - mRectF.top) / scale
        val srcBmp = drawable.bitmap
        val cropBmp = Bitmap.createBitmap(
            srcBmp,
            left.toInt(),
            top.toInt(),
            w.toInt(),
            h.toInt()
        )
        val bmp = Bitmap.createScaledBitmap(cropBmp, outputX, outputY, false)
        cropBmp.recycle()
        return bmp
    }


}