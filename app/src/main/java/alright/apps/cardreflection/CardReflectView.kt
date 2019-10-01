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
    private var finalBitmap: Bitmap? = null

    override val coroutineContext: CoroutineContext =
        Dispatchers.Main + SupervisorJob()

    init {
        //Allows the BlurMaskFilter below to function
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        inflate(context, R.layout.card_reflect_view, null)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CardReflectView)
        reflectDistance = attributes.getDimension(R.styleable.CardReflectView_reflect_distance, 0F).toInt().px
        reflectSize = attributes.getDimension(R.styleable.CardReflectView_reflect_size, 0F).toInt().px
        reflectImageResource = attributes.getResourceId(R.styleable.CardReflectView_reflect_image, 0)
        attributes.recycle()
    }

    fun setCardImage(newImageResource: Int) {
        reflectImageResource = newImageResource
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)

        if(reflectImageResource != 0 && canvas != null){
            Log.d(tag, "onDraw, drawing bitmap....")
            val startTime = System.currentTimeMillis()

            val bitmap = BitmapFactory.decodeResource(resources, reflectImageResource)
            val width = canvas.width
            val height = (canvas.height - reflectDistance)/2
            val centerCropBitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height)

            drawRoundedBitmap(centerCropBitmap, canvas)

            val blurredBitmap = BlurBuilder.blur(context, centerCropBitmap)
            drawMirrorBitmap(blurredBitmap, canvas)

            Log.d(tag, "Transformation took: " + (System.currentTimeMillis() - startTime) + " millis to complete!")
        }
    }

    private fun drawRoundedBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = (canvas.height.toFloat() - reflectDistance)/2

        val roundRect = RectF(0F, 0F, width, height)
        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        //mPaint.style = Paint.Style.FILL
        //canvas.drawRoundRect(roundRect, 20F, 20F, mPaint)

        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mPaint.shader = shader
        canvas.drawRoundRect(roundRect, 20F, 20F, mPaint)
        mPaint.shader = null
    }

    private fun drawMirrorBitmap(bitmap: Bitmap, canvas: Canvas){

        val width = canvas.width.toFloat()
        val height = ((canvas.height.toFloat())/2) + reflectDistance

        val roundRect = RectF(0F, height, width, height + reflectSize)
        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        //Attempt to add transparency gradient
        val colors = IntArray(3)
        colors[0] = Color.argb(127, 0, 0, 0)
        colors[1] = Color.argb(63, 0, 0, 0)
        colors[2] = Color.argb(0, 0, 0, 0)

        val positions = FloatArray(3)
        positions[0] = 0f
        positions[1] = 0.5f
        positions[2] = 1f

        val shaderA = LinearGradient(0F, height, 0F, height + reflectSize.toFloat(), colors, positions, Shader.TileMode.CLAMP)
        val shaderB = BitmapShader(bitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
        val paint = Paint()
        paint.shader = ComposeShader(shaderA, shaderB, PorterDuff.Mode.SRC_IN)
        //paint.maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawRoundRect(roundRect, 20f, 20f, paint)

        mPaint.shader = null
    }

    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}