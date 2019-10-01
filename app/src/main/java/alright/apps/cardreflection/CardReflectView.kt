package alright.apps.cardreflection

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class CardReflectView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), CoroutineScope {

    private val tag = "CardReflectView"
    private var imageResource: Int = 0
    private var reflectDistance: Int = 0
    private var finalBitmap: Bitmap? = null

    override val coroutineContext: CoroutineContext =
        Dispatchers.Main + SupervisorJob()

    init {
        //Allows the BlurMaskFilter below to function
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        inflate(context, R.layout.card_reflect_view, this)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CardReflectView)
        reflectDistance = attributes.getDimension(R.styleable.CardReflectView_reflect_distance, 0F).toInt().px
        imageResource = attributes.getResourceId(R.styleable.CardReflectView_image, 0)
        attributes.recycle()
    }

    fun setCardImage(newImageResource: Int) {

        imageResource = newImageResource

        invalidate()

        launch {
            withContext(Dispatchers.Default) {

                /*val roundedBitmap = originalImage.drawToBitmap()

                //Next we grab a copy and rotate it
                val m = Matrix()
                m.preScale(1f, -1f)
                val mirrorBitmap =
                    Bitmap.createBitmap(
                        roundedBitmap,
                        0,
                        0,
                        roundedBitmap.width,
                        roundedBitmap.height,
                        m,
                        false
                    )

                //For an efficiency attempt, we chop some this bitmap down
                val pixels = IntArray(mirrorBitmap.width * mirrorHeight)
                mirrorBitmap.getPixels(
                    pixels,
                    0,
                    mirrorBitmap.width,
                    0,
                    0,
                    mirrorBitmap.width,
                    mirrorHeight
                )

                val croppedBitmap = Bitmap.createBitmap(
                    mirrorBitmap.width,
                    mirrorHeight,
                    Bitmap.Config.ARGB_8888
                )

                //For some reason, the cropped pixels need to be swizzled:
                //https://stackoverflow.com/questions/47970384/why-is-copypixelsfrombuffer-giving-incorrect-color-setpixels-is-correct-but-slo
                for (i in pixels.indices) {
                    val red = Color.red(pixels[i])
                    val green = Color.green(pixels[i])
                    val blue = Color.blue(pixels[i])
                    val alpha = Color.alpha(pixels[i])
                    pixels[i] = Color.argb(alpha, blue, green, red)
                }
                croppedBitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixels))

                //Blur the cropped, mirrored bitmap
                //https://medium.com/@ssaurel/create-a-blur-effect-on-android-with-renderscript-aa05dae0bd7d
                val blurredBitmap = BlurBuilder.blur(context, croppedBitmap)

                //Add a transparency gradient to the blurred image
                finalBitmap = addGradient(blurredBitmap)

                withContext(Dispatchers.Main) {
                    resultImage.setImageBitmap(finalBitmap)

                    //Request layout to draw these results to the screen
                    Log.d(tag, "Requesting layout....")
                    requestLayout()
                }

                Log.d(
                    tag,
                    "Transformation took: " + (System.currentTimeMillis() - startTime) + " millis to complete!"
                )*/
            }

        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)

        if(imageResource != 0 && canvas != null){
            Log.d(tag, "onDraw, drawing bitmap....")
            val startTime = System.currentTimeMillis()

            val bitmap = BitmapFactory.decodeResource(resources, imageResource)

            val width = canvas.width
            val height = (canvas.height - reflectDistance)/2

            val centerCropBitmap = ThumbnailUtils.extractThumbnail(bitmap, width,
                height)

            drawRoundedBitmap(centerCropBitmap, canvas)

            val blurredBitmap = BlurBuilder.blur(context, centerCropBitmap)
            drawMirrorBitmap(blurredBitmap, canvas)
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

        val roundRect = RectF(0F, height, width, height*2)
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

        val shaderA = LinearGradient(0F, height, 0F, canvas.height.toFloat(), colors, positions, Shader.TileMode.CLAMP)
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