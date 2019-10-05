package alright.apps.cardreflection

import android.content.Context
import android.graphics.Bitmap
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur
import kotlin.math.roundToInt


object BlurBuilder {
    private const val BITMAP_SCALE = 0.33f
    private const val BLUR_RADIUS = 8f

    fun blur(context: Context, image: Bitmap): Bitmap {
        val width = (image.width * BITMAP_SCALE).roundToInt()
        val height = (image.height * BITMAP_SCALE).roundToInt()

        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)

        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        intrinsicBlur.setRadius(BLUR_RADIUS)
        intrinsicBlur.setInput(tmpIn)
        intrinsicBlur.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)

        return Bitmap.createScaledBitmap(outputBitmap, image.width, image.height, false)
    }
}