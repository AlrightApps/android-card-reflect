package alright.apps.reflectivecard

import android.content.Context
import android.graphics.*
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.toRect
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
    private val blurMaskFilter = BlurMaskFilter(32f, BlurMaskFilter.Blur.NORMAL)

    init {
        //Allows the BlurMaskFilter below to function
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CardReflectView)
        reflectImageResource = attributes.getResourceId(R.styleable.CardReflectView_reflect_image, 0)
        reflectCornerRadius = attributes.getDimension(R.styleable.CardReflectView_reflect_corner_radius, 0F)
        reflectElevation = attributes.getDimension(R.styleable.CardReflectView_reflect_elevation, 0f)
        reflectSize = attributes.getDimension(R.styleable.CardReflectView_reflect_size, 0f)
        reflectSidePadding = attributes.getDimension(R.styleable.CardReflectView_reflect_image_side_padding, 0f)
        attributes.recycle()

        transparencyLevels = IntArray(2)
        transparencyLevels[0] = Color.argb(127, 0, 0, 0)
        transparencyLevels[1] = Color.argb(0, 0, 0, 0)

        transparencyPositions = FloatArray(2)
        transparencyPositions[0] = 0f
        transparencyPositions[1] = 1f
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
            val height = canvas.height
            val centerCroppedBitmap = ThumbnailUtils.extractThumbnail(image, width, height)

            drawCardBitmap(centerCroppedBitmap, canvas)

            val matrix = Matrix()
            matrix.setScale(1f, -1f)
            val mirroredBitmap = Bitmap.createBitmap(centerCroppedBitmap, 0, (height - (reflectElevation - reflectSize - reflectSize).absoluteValue).toInt(), width, reflectSize.toInt(), matrix, false)
            drawReflectionBitmap(mirroredBitmap, canvas)

            Log.d(tag, "Transformation took: " + (System.currentTimeMillis() - startTime) + " millis to complete!")
        }
    }

    private fun drawCardBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat() - reflectElevation - reflectSize

        val roundRect = RectF(reflectSidePadding, 0F, width - reflectSidePadding, height)

        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        canvas.drawRoundRect(roundRect, reflectCornerRadius, reflectCornerRadius, paint)
        paint.shader = null
    }

    private fun drawReflectionBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val sourceRect = RectF(reflectSidePadding, 0f, width - reflectSidePadding, bitmap.height.toFloat())
        val destinationRect = RectF(reflectSidePadding, height - bitmap.height.toFloat(), width - reflectSidePadding, height)

        //Attempt to get roundRect bitmap separately
        val roundRect = RectF(reflectSidePadding, reflectSidePadding, width - reflectSidePadding, bitmap.height.toFloat())
        val roundRectBitmap = Bitmap.createBitmap(width.toInt(), bitmap.height + (reflectSidePadding).toInt(), Bitmap.Config.ARGB_8888)
        val roundRectPaint = Paint()
        val roundRectCanvas = Canvas(roundRectBitmap)
        roundRectPaint.isAntiAlias = true
        roundRectPaint.color = Color.BLACK
        roundRectPaint.style = Paint.Style.FILL
        roundRectCanvas.drawARGB(0, 0, 0, 0)
        roundRectCanvas.drawRoundRect(roundRect, reflectCornerRadius, reflectCornerRadius, roundRectPaint)

        roundRectPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        roundRectCanvas.drawBitmap(bitmap, sourceRect.toRect(), roundRect.toRect(), roundRectPaint)

        val blurred = BlurBuilder.blur(context, roundRectBitmap)
        val blurredRect = Rect(0, 0, blurred.width, blurred.height)
        val blurredDestRect = Rect(0, height.toInt() - bitmap.height, width.toInt(), height.toInt())

        canvas.drawBitmap(blurred, blurredRect, blurredDestRect, paint)

        paint.shader = null
        //paint.maskFilter = null
    }
}