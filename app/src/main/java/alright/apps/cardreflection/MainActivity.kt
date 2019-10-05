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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    private var photosList: ArrayList<Int> = ArrayList()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler_view.layoutManager = linearLayoutManager

        photosList.addAll(
            listOf(
                R.drawable.nasa_1,
                R.drawable.nasa_2,
                R.drawable.nasa_3,
                R.drawable.nasa_4,
                R.drawable.nasa_5,
                R.drawable.nasa_6,
                R.drawable.nasa_7,
                R.drawable.nasa_8,
                R.drawable.nasa_9,
                R.drawable.nasa_10,
                R.drawable.nasa_11,
                R.drawable.nasa_12,
                R.drawable.random_13,
                R.drawable.random_14,
                R.drawable.random_15,
                R.drawable.random_16,
                R.drawable.random_17,
                R.drawable.random_18
            )
        )
        photosList.shuffle()
        adapter = RecyclerAdapter(photosList)
        recycler_view.adapter = adapter
    }

}