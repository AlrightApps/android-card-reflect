/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package alright.apps.cardreflection

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*

class RecyclerAdapter(private val photos: ArrayList<Photo>) : RecyclerView.Adapter<RecyclerAdapter.PhotoHolder>() {

  fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
    val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
    return PhotoHolder(inflatedView)
  }

  override fun getItemCount(): Int = photos.size

  override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
    val itemPhoto = photos[position]
    holder.bindPhoto(itemPhoto)
  }

  //1
  class PhotoHolder(private val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
    //2
    private var photo: Photo? = null

    //3
    init {
      view.setOnClickListener(this)
    }

    fun bindPhoto(photo: Photo) {
      this.photo = photo

      val imageResource = when (adapterPosition) {
        0 -> R.drawable.nasa_1
        1 -> R.drawable.nasa_2
        2 -> R.drawable.nasa_3
        3 -> R.drawable.nasa_4
        4 -> R.drawable.nasa_5
        5 -> R.drawable.nasa_6
        6 -> R.drawable.nasa_7
        7 -> R.drawable.nasa_8
        8 -> R.drawable.nasa_9
        9 -> R.drawable.nasa_10
        10 -> R.drawable.nasa_11
        else -> R.drawable.nasa_12
      }

      view.card_image.setCardImage(imageResource)
    }

    //4
    override fun onClick(v: View) {
      Log.d("RecyclerView", "CLICK!")
    }

    companion object {
      //5
      private val PHOTO_KEY = "PHOTO"
    }
  }

}