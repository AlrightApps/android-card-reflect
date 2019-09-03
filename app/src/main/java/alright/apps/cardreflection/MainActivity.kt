package alright.apps.cardreflection

import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.View.MeasureSpec
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.IntBuffer
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.LinearGradient
import android.graphics.Bitmap




class MainActivity : AppCompatActivity() {

    val MIRROR_HEIGHT = 250

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val resultBmp = BlurBuilder.blur(this, originalImage.getBitmap())//BitmapFactory.decodeResource(resources, R.drawable.icecream))

        val drawable = RoundedBitmapDrawableFactory.create(resources, BitmapFactory.decodeResource(resources, R.drawable.icecream))
        drawable.cornerRadius = 24.px.toFloat()
        originalImage.setImageDrawable(drawable)

        val bitmap = originalImage.getBitmap()
        val m = Matrix()
        m.postScale(1f, -1f, bitmap.width/2f, bitmap.height/2f)
        val mirrorBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, false)

        //resultImage.setImageBitmap(bitmap)

        val pixels = IntArray(mirrorBitmap.width * MIRROR_HEIGHT)
        mirrorBitmap.getPixels(pixels, 0, mirrorBitmap.width, 0, 0, mirrorBitmap.width, MIRROR_HEIGHT)

        val bitmapRow = Bitmap.createBitmap(mirrorBitmap.width, MIRROR_HEIGHT, Bitmap.Config.ARGB_8888)

        for (i in 0 until pixels.size) {
            val red = Color.red(pixels[i])
            val green = Color.green(pixels[i])
            val blue = Color.blue(pixels[i])
            val alpha = Color.alpha(pixels[i])
            pixels[i] = Color.argb(alpha, blue, green, red)
        }
        bitmapRow.copyPixelsFromBuffer(IntBuffer.wrap(pixels))

        val finalBlurredImage = BlurBuilder.blur(this, bitmapRow)

        val gradientized = addGradient(finalBlurredImage)
        resultImage.setImageBitmap(gradientized)
    }

    fun View.getBitmap(): Bitmap {
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

    fun addGradient(src: Bitmap): Bitmap {
        val w = src.width.toFloat()
        val h = src.height.toFloat()
        val overlay = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(overlay)

        canvas.drawBitmap(src, 0f, 0f, null)

        val paint = Paint()
        val colors = IntArray(3)
        colors[0] = 0x40FFFFFF
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
