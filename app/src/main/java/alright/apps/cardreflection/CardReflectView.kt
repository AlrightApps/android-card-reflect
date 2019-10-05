package alright.apps.cardreflection

import android.content.Context
import android.graphics.*
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View


class CardReflectView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val tag = "CardReflectView"
    private var reflectImageResource: Int = 0
    private var reflectDistance = 0f
    private var reflectSize = 0f
    private var reflectSidePadding = 0f
    private var reflectCornerRadius = 0f
    private var image: Bitmap? = null

    private var gradientColors: IntArray
    private var gradientPositions: FloatArray

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blurMaskFilter = BlurMaskFilter(32f, BlurMaskFilter.Blur.NORMAL)

    init {
        //Allows the BlurMaskFilter below to function
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CardReflectView)
        reflectImageResource = attributes.getResourceId(R.styleable.CardReflectView_reflect_image, 0)
        reflectDistance = attributes.getDimension(R.styleable.CardReflectView_reflect_distance, 0f)
        reflectSize = attributes.getDimension(R.styleable.CardReflectView_reflect_size, 0f)
        reflectSidePadding = attributes.getDimension(R.styleable.CardReflectView_reflect_image_side_padding, 0f)
        reflectCornerRadius = attributes.getDimension(R.styleable.CardReflectView_reflect_corner_radius, 0F)
        attributes.recycle()

        gradientColors = IntArray(2)
        gradientColors[0] = Color.argb(127, 0, 0, 0)
        gradientColors[1] = Color.argb(0, 0, 0, 0)

        gradientPositions = FloatArray(2)
        gradientPositions[0] = 0f
        gradientPositions[1] = 1f
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

            val width = canvas.width - (reflectSidePadding*2)
            val height = canvas.height - reflectDistance - reflectSize
            val centerCroppedBitmap = ThumbnailUtils.extractThumbnail(image, width.toInt(), height.toInt())

            drawCardBitmap(centerCroppedBitmap, canvas)
            //Take care not to blur the image too much, as a BlurMaskFilter is also applied to give the edges some fuzziness
            val blurredBitmap = BlurBuilder.blur(context, centerCroppedBitmap)
            drawReflectionBitmap(blurredBitmap, canvas)

            Log.d(tag, "Transformation took: " + (System.currentTimeMillis() - startTime) + " millis to complete!")
        }
    }

    private fun drawCardBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat() - reflectDistance - reflectSize

        val roundRect = RectF(reflectSidePadding, 0F, width - reflectSidePadding, height)

        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        canvas.drawRoundRect(roundRect, reflectCornerRadius, reflectCornerRadius, paint)
        paint.shader = null
    }

    private fun drawReflectionBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val shaderA = LinearGradient(0F, height - reflectSize, 0F, height, gradientColors, gradientPositions, Shader.TileMode.CLAMP)
        val shaderB = BitmapShader(bitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
        paint.shader = ComposeShader(shaderA, shaderB, PorterDuff.Mode.SRC_IN)
        paint.maskFilter = blurMaskFilter

        val rect = RectF(reflectSidePadding, height - reflectSize, width - reflectSidePadding, height)
        canvas.drawRoundRect(rect, reflectCornerRadius, reflectCornerRadius, paint)

        paint.shader = null
        paint.maskFilter = null
    }
}