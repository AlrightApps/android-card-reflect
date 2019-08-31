package alright.apps.cardreflection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.BitmapFactory
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val resultBmp = BlurBuilder.blur(this, BitmapFactory.decodeResource(resources, R.drawable.icecream))
        resultImage.setImageBitmap(resultBmp)
    }
}
