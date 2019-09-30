package alright.apps.cardreflection

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.BitmapShader
import android.graphics.ComposeShader
import android.R.attr.bitmap
import android.opengl.ETC1.getWidth
import android.graphics.LinearGradient
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View


class CardReflectView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), CoroutineScope {

    private val tag = "CardReflectView"
    private var imageResource: Int = 0
    private var mirrorHeight: Int = 0
    private var finalBitmap: Bitmap? = null

    override val coroutineContext: CoroutineContext =
        Dispatchers.Main + SupervisorJob()

    init {
        inflate(context, R.layout.card_reflect_view, this)

        this.isDrawingCacheEnabled = true

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CardReflectView)
        mirrorHeight =
            attributes.getDimension(R.styleable.CardReflectView_mirror_height, 0F).toInt().px
        imageResource = attributes.getResourceId(R.styleable.CardReflectView_image, 0)
        attributes.recycle()
    }

    fun get(): Bitmap {
        return this.drawingCache
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
            val centerCropBitmap = ThumbnailUtils.extractThumbnail(bitmap, 500, 500)
            drawRoundedBitmap(centerCropBitmap, canvas)

            drawMirrorBitmap(centerCropBitmap, canvas)

        }
    }

    private fun drawRoundedBitmap(bitmap: Bitmap, canvas: Canvas){

        val roundRect = RectF(0F, 0F, 500F, 500F)
        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        mPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(roundRect, 20F, 20F, mPaint)

        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mPaint.shader = shader
        canvas.drawRoundRect(roundRect, 20F, 20F, mPaint)
        mPaint.shader = null
    }

    private fun drawMirrorBitmap(bitmap: Bitmap, canvas: Canvas){

        val roundRect = RectF(0F, 500F, 500F, 1000F)
        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        mPaint.style = Paint.Style.FILL
        //canvas.drawRoundRect(roundRect, 20F, 20F, mPaint)
        val corners = floatArrayOf(
            20f, 20f,   // Top left radius in px
            20f, 20f,   // Top right radius in px
            0f, 0f,     // Bottom right radius in px
            0f, 0f      // Bottom left radius in px
        )

        var path = Path()
        path.addRoundRect(roundRect, corners, Path.Direction.CW)
        canvas.drawPath(path, mPaint)

        val blurredBitmap = BlurBuilder.blur(context, bitmap)

        val shader = BitmapShader(blurredBitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
        mPaint.shader = shader
        path = Path()
        path.addRoundRect(roundRect, corners, Path.Direction.CW)
        canvas.drawPath(path, mPaint)

        //Attempt to add transparency gradient
/*        val colors = IntArray(3)
        colors[0] = 0x50FFFFFF
        colors[1] = 0x20FFFFFF
        colors[2] = 0x00FFFFFF

        val positions = FloatArray(3)
        positions[0] = 0f
        positions[1] = 0.5f
        positions[2] = 1f

        val shaderA = LinearGradient(0F, 0F, 500F, 0F, colors, positions, Shader.TileMode.MIRROR)
        val shaderB = BitmapShader(blurredBitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
        val paint = Paint()
        paint.shader = ComposeShader(shaderA, shaderB, PorterDuff.Mode.SRC_IN)
        canvas.drawRect(roundRect, paint)*/

        mPaint.shader = null
    }

    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun addGradient(src: Bitmap, canvas: Canvas) {
        val shaderA = LinearGradient(0F, 0F, 500F, 0F, 0x50FFFFFF, 0x00ffffff, Shader.TileMode.CLAMP)
        val shaderB = BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val paint = Paint()
        paint.shader = ComposeShader(shaderA, shaderB, PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0F, 0F, 500F, 500F, paint)
    }
}