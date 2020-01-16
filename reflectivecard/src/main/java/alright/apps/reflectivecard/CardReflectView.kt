package alright.apps.reflectivecard

import android.content.Context
import android.graphics.*
import android.graphics.Shader.TileMode
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.absoluteValue


class CardReflectView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val tag = "CardReflectView"
    private var reflectImageResource: Int = 0
    private var reflectElevation = 0f
    private var reflectSize = 0f
    private var reflectSidePadding = 0f
    private var reflectCornerRadius = 0f
    private var image: Bitmap? = null

    private var transparencyLevels: IntArray
    private var transparencyPositions: FloatArray

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CardReflectView)
        reflectImageResource = attributes.getResourceId(R.styleable.CardReflectView_reflect_image, 0)
        reflectCornerRadius = attributes.getDimension(R.styleable.CardReflectView_reflect_corner_radius, 0F)
        reflectElevation = attributes.getDimension(R.styleable.CardReflectView_reflect_elevation, 0f)
        reflectSize = attributes.getDimension(R.styleable.CardReflectView_reflect_size, 0f)
        reflectSidePadding = attributes.getDimension(R.styleable.CardReflectView_reflect_image_side_padding, 0f)
        attributes.recycle()

        transparencyLevels = IntArray(3)
        transparencyLevels[0] = Color.argb(200, 0, 0, 0)
        transparencyLevels[1] = Color.argb(75, 0, 0, 0)
        transparencyLevels[2] = Color.argb(0, 0, 0, 0)

        transparencyPositions = FloatArray(3)
        transparencyPositions[0] = 0f
        transparencyPositions[1] = 0.3f
        transparencyPositions[2] = 1f
    }

    fun setCardImage(newImageResource: Int) {
        reflectImageResource = newImageResource
        image = BitmapFactory.decodeResource(resources, reflectImageResource)
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)

        if(reflectImageResource != 0 && canvas != null){
            Log.d(tag, "onDraw, drawing bitmap....")
            val startTime = System.currentTimeMillis()

            val width = canvas.width
            val height = canvas.height - reflectSize.toInt()
            val centerCroppedBitmap = ThumbnailUtils.extractThumbnail(image, width, height)

            drawCardBitmap(centerCroppedBitmap, canvas)

            val matrix = Matrix()
            matrix.setScale(1f, -1f)
            val mirroredBitmap = Bitmap.createBitmap(centerCroppedBitmap, 0, (height - reflectSize.toInt()), width, reflectSize.toInt(), matrix, false)
            drawReflectionBitmap(mirroredBitmap, canvas)

            Log.d(tag, "Transformation took: " + (System.currentTimeMillis() - startTime) + " millis to complete!")
        }
    }

    private fun drawCardBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat() - reflectSize

        val roundRect = RectF(reflectSidePadding, 0F, width - reflectSidePadding, height)

        val shader = BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP)
        paint.shader = shader
        canvas.drawRoundRect(roundRect, reflectCornerRadius, reflectCornerRadius, paint)
        paint.shader = null
    }

    private fun drawReflectionBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val roundRect = RectF(reflectSidePadding, 0F, bitmap.width - reflectSidePadding, bitmap.height.toFloat())
        val roundRectBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val roundRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val roundRectCanvas = Canvas(roundRectBitmap)

        roundRectPaint.color = Color.BLACK
        roundRectPaint.style = Paint.Style.FILL

        roundRectCanvas.drawARGB(0, 0, 0, 0)
        roundRectCanvas.drawRoundRect(roundRect, reflectCornerRadius, reflectCornerRadius, roundRectPaint)

        roundRectPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val gradientized = addGradient(bitmap)

        roundRectCanvas.drawBitmap(gradientized, 0F, 0F, roundRectPaint)

        val blurred = roundRectBitmap //BlurBuilder.blur(context, roundRectBitmap)
        val blurredRect = Rect(0, 0, blurred.width, blurred.height)
        val blurredDestRect = Rect(0, height.toInt() - bitmap.height, width.toInt(), height.toInt())

        canvas.drawBitmap(blurred, blurredRect, blurredDestRect, paint)

        paint.shader = null
        //paint.maskFilter = null
    }

    fun addGradient(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(overlay)
        canvas.drawBitmap(src, 0F, 0F, null)

        //val paint = Paint()
        //val shader = LinearGradient(0F, 0F, 0F, h.toFloat(), transparencyLevels, transparencyPositions, TileMode.CLAMP)
        //paint.shader = shader
        //paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        //canvas.drawRect(0F, 0F, w.toFloat(), h.toFloat(), paint)

        return overlay
    }
}