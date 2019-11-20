package com.asksira.flexibletablayoutdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import java.lang.IllegalArgumentException

class JustAPageAdapter: PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return 4
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val color = ContextCompat.getColor(container.context, when (position) {
            0 -> android.R.color.holo_red_light
            1 -> android.R.color.holo_orange_light
            2 -> android.R.color.holo_green_light
            3 -> android.R.color.holo_blue_light
            else -> throw IllegalArgumentException("Unexpected page position")
        })
        val view = LayoutInflater.from(container.context).inflate(R.layout.item_page, container, false)
        view.setBackgroundColor(color)
        view.findViewById<TextView>(R.id.tvPageNumber).text = (position+1).toString()
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}