package alright.apps.cardreflection

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class CardReflectView(context: Context, attrs: AttributeSet) : View(context, attrs), CoroutineScope {

    private val tag = "CardReflectView"
    private var reflectImageResource: Int = 0
    private var reflectDistance: Int = 0
    private var reflectSize: Int = 0
    private var reflectCornerRadius: Float = 0f
    private var image: Bitmap? = null

    override val coroutineContext: CoroutineContext =
        Dispatchers.Main + SupervisorJob()

    init {
        //Allows the BlurMaskFilter below to function
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        inflate(context, R.layout.card_reflect_view, null)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CardReflectView)
        reflectDistance = attributes.getDimension(R.styleable.CardReflectView_reflect_distance, 0F).px
        reflectSize = attributes.getDimension(R.styleable.CardReflectView_reflect_size, 0F).px
        reflectImageResource = attributes.getResourceId(R.styleable.CardReflectView_reflect_image, 0)
        reflectCornerRadius = attributes.getDimension(R.styleable.CardReflectView_reflect_corner_radius, 0F).px.toFloat()
        attributes.recycle()
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

            val width = canvas.width - 64
            val height = canvas.height - reflectDistance - reflectSize
            val centerCropBitmap = ThumbnailUtils.extractThumbnail(image, width, height)

            drawRoundedBitmap(centerCropBitmap, canvas)

            val blurredBitmap = BlurBuilder.blur(context, centerCropBitmap)
            drawMirrorBitmap(blurredBitmap, canvas)

            Log.d(tag, "Transformation took: " + (System.currentTimeMillis() - startTime) + " millis to complete!")
        }
    }

    private fun drawRoundedBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat() - reflectDistance - reflectSize

        val roundRect = RectF(32F, 0F, width - 32F, height)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        canvas.drawRoundRect(roundRect, reflectCornerRadius, reflectCornerRadius, paint)
        paint.shader = null
    }

    private fun drawMirrorBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val roundRect = RectF(32F, height - reflectSize, width - 32F, height)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colors = IntArray(2)
        colors[0] = Color.argb(127, 0, 0, 0)
        colors[1] = Color.argb(0, 0, 0, 0)

        val positions = FloatArray(2)
        positions[0] = 0f
        positions[1] = 1f

        val shaderA = LinearGradient(0F, height - reflectSize, 0F, height, colors, positions, Shader.TileMode.CLAMP)
        val shaderB = BitmapShader(bitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
        paint.shader = ComposeShader(shaderA, shaderB, PorterDuff.Mode.SRC_IN)
        paint.maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawRoundRect(roundRect, reflectCornerRadius, reflectCornerRadius, paint)

        paint.shader = null
    }

    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Float.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}