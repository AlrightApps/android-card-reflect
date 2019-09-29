package alright.apps.cardreflection

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlinx.android.synthetic.main.card_reflect_view.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope
import java.nio.IntBuffer
import kotlin.coroutines.CoroutineContext

class CardReflectView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), CoroutineScope {

    private val tag = "CardReflectView"
    private var imageResource: Int = 0
    private var mirrorHeight: Int = 0
    private var finalBitmap: Bitmap? = null

    override val coroutineContext: CoroutineContext =
        Dispatchers.Main + SupervisorJob()

    init {
        inflate(context, R.layout.card_reflect_view, this)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CardReflectView)
        mirrorHeight =
            attributes.getDimension(R.styleable.CardReflectView_mirror_height, 0F).toInt().px
        imageResource = attributes.getResourceId(R.styleable.CardReflectView_image, 0)
        attributes.recycle()
    }

    fun setCardImage(newImageResource: Int) {
        launch {
            withContext(Dispatchers.Default) {

                Log.d(tag, "Beginning transform....")
                imageResource = newImageResource

                //First we take the bitmap and round the corners of it, so it looks tasty. We use this function to center crop the image
                //https://stackoverflow.com/a/51702442/1866373
                val roundedDrawable = getRoundedBitmapDrawable(
                    context,
                    BitmapFactory.decodeResource(resources, imageResource)
                )

                val startTime = System.currentTimeMillis()

                //withContext(Dispatchers.Main) {
                    originalImage.setImageDrawable(roundedDrawable)
                //}

                //Next we grab a copy and rotate it
                val bitmap = originalImage.getBitmap()
                val m = Matrix()
                m.preScale(1f, -1f)
                val mirrorBitmap =
                    Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
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
                //withContext(Dispatchers.Main) {
                    resultImage.setImageBitmap(finalBitmap)
                //}

                Log.d(
                    tag,
                    "Transformation took: " + (System.currentTimeMillis() - startTime) + " millis to complete!"
                )
            }

            //Request layout to draw these results to the screen
            Log.d(tag, "Requesting layout....")
            requestLayout()
        }
    }

    private fun getRoundedBitmapDrawable(context: Context, bitmap: Bitmap): RoundedBitmapDrawable {
        val size = bitmap.width.coerceAtMost(bitmap.height)
        val centerCropBitmap = ThumbnailUtils.extractThumbnail(bitmap, size, size)
        val roundedBitmapDrawable =
            RoundedBitmapDrawableFactory.create(context.resources, centerCropBitmap)
        roundedBitmapDrawable.isFilterBitmap = true
        roundedBitmapDrawable.setAntiAlias(true)
        roundedBitmapDrawable.cornerRadius = 24.px.toFloat()
        return roundedBitmapDrawable
    }

    private fun View.getBitmap(): Bitmap {
        val specWidth = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        measure(specWidth, specWidth)
        val b = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        layout(0, 0, measuredWidth, measuredHeight)
        draw(c)
        return b
    }

    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun addGradient(src: Bitmap): Bitmap {
        val w = src.width.toFloat()
        val h = src.height.toFloat()
        val overlay = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(overlay)

        canvas.drawBitmap(src, 0f, 0f, null)

        val paint = Paint()
        val colors = IntArray(3)
        colors[0] = 0x50FFFFFF
        colors[1] = 0x20FFFFFF
        colors[2] = 0x00FFFFFF

        val positions = FloatArray(3)
        positions[0] = 0f
        positions[1] = 0.5f
        positions[2] = 1f
        val shader = LinearGradient(0f, 0f, 0f, h, colors, positions, Shader.TileMode.REPEAT)
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawRect(0f, h - h, w, h, paint)

        return overlay
    }
}