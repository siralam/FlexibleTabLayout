package com.asksira.flexibletablayoutdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {

    var adapter = JustAPageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vp.adapter = adapter
        vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                ftl.onPageScrolled(position, positionOffset)
            }

            override fun onPageSelected(position: Int) {
                ftl.onPageSelected(position)
            }
        })

        ftl.inflateTabsDelegate = { container ->
            val list: ArrayList<View> = arrayListOf()
            (1..4).forEach { i ->
                val tab = LayoutInflater.from(container.context).inflate(R.layout.item_tab, container, false)
                tab.findViewById<TextView>(R.id.tvTabName).text = when (i) {
                    1 -> "A1"
                    2 -> "BB2"
                    3 -> "CCC3"
                    4 -> "Remaining"
                    else -> throw IllegalArgumentException("Unexpected page position")
                }
                val color = ContextCompat.getColor(container.context, when (i) {
                    1 -> android.R.color.holo_red_light
                    2 -> android.R.color.holo_orange_light
                    3 -> android.R.color.holo_green_light
                    4 -> android.R.color.holo_blue_light
                    else -> throw IllegalArgumentException("Unexpected page position")
                })
                tab.setBackgroundColor(color)
                tab.setOnClickListener {
                    vp.currentItem = i - 1
                }
                if (i == 4) { //Make the last tab fill remaining width
                    tab.layoutParams.width = 0
                    (tab.layoutParams as LinearLayout.LayoutParams).weight = 1f
                }
                list.add(tab)
            }
            list.toList()
        }
        ftl.inflateIndicatorDelegate = { container ->
            val indicator = LayoutInflater.from(container.context).inflate(R.layout.item_indicator, container, false)
            indicator
        }
        ftl.onTabProgressUpdate = { tabs, progresses ->
            tabs.forEachIndexed { index, view ->
                view.findViewById<TextView>(R.id.tvTabName).alpha = progresses[index]
            }
        }

        ftl.inflateTabs()
        ftl.inflateIndicator()
    }
}
