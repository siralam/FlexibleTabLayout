package com.asksira.flexibletablayout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import java.lang.IllegalStateException

class FlexibleTabLayout : LinearLayout {

    private lateinit var llTabs: LinearLayout
    private lateinit var flIndicatorContainer: FrameLayout

    var tabs: List<View> = listOf()
    var indicator: View? = null

    private var selectedPosition = 0

    /**
     * User should implement this delegate to provide their custom tab views.
     * They should also provide their LinearLayoutParams in order to determine how their tabs are laid out,
     * e.g. Whether they have the same width (width 0 weight 1) or they have their own special width.
     * (Note that this can be done in XML. No need to use code to provide LinearLayoutParams.)
     * Do NOT add the created views to the container. This will be done internally by FlexibleTabLayout.
     */
    var inflateTabsDelegate: ((container: LinearLayout) -> List<View>)? = null

    /**
     * User should implement this delegate to provide their custom indicator view.
     * No need to specify the position or width (but height is necessary) as the position and width will be
     * controlled by FlexibleTabLayout.
     */
    var inflateIndicatorDelegate: ((container: FrameLayout) -> View)? = null

    /**
     * This is by nature the same as onPageScrolled; but another style of callback to user,
     * So that it is easier for user to perform corresponding actions on tab simply based on their individual progress.
     * The progresses here maps its index to the index of the tab in tabs,
     * i.e. progresses[0] will be the progress of tabs[0].
     */
    var onTabProgressUpdate: ((tabs: List<View>, progresses: List<Float>) -> Unit)? = null

    private var _hasIndicator = true
    var hasIndicator: Boolean
        get() = _hasIndicator
        set(value) {
            _hasIndicator = value
            flIndicatorContainer.isVisible = value
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FlexibleTabLayout,
            defStyleAttr,
            0
        )
        try {
            _hasIndicator = a.getBoolean(R.styleable.FlexibleTabLayout_has_indicator, true)
        } finally {
            a.recycle()
        }
        orientation = VERTICAL
        inflate(context, R.layout.widget_flexible_tab_layout, this)
        llTabs = findViewById(R.id.llTabsContainer)
        flIndicatorContainer = findViewById(R.id.flIndicatorContainer)
        flIndicatorContainer.isVisible = hasIndicator
    }

    fun inflateTabs() {
        llTabs.removeAllViews()
        tabs = inflateTabsDelegate?.invoke(llTabs) ?: listOf()
        tabs.forEach { llTabs.addView(it) }
    }

    fun inflateIndicator() {
        flIndicatorContainer.removeAllViews()
        indicator = inflateIndicatorDelegate?.invoke(flIndicatorContainer)
        indicator?.let { flIndicatorContainer.addView(it) }
        indicator?.afterMeasured {
            tabs.getOrNull(selectedPosition)?.let { selectedTab ->
                indicator?.layoutParams?.width = selectedTab.width
                indicator?.requestLayout()
            }
        }
    }

    /**
     * ViewPager should add OnPageChange Listener and call this method in onPageScrolled.
     */
    fun onPageScrolled(position: Int, positionOffset: Float) {
        if (hasIndicator) evaluateIndicatorProgress(position, positionOffset)
        evaluateTabProgress(position, positionOffset)
    }

    /**
     * ViewPager should add OnPageChange Listener and call this method in onPageSelected.
     */
    fun onPageSelected(newPosition: Int) {
        selectedPosition = newPosition
    }

    private fun evaluateTabProgress(position: Int, positionOffset: Float) {
        val progresses = if (positionOffset == 0f) { //Which means it reaches the end, special case
            tabs.mapIndexed { index, _ ->
                if (index == position) 1f else 0f
            }
        } else { //Still scrolling
            tabs.mapIndexed { index, _ ->
                when {
                    index < position || index > position + 1 -> 0f //Not related to this action
                    index == position -> 1 - positionOffset
                    index == position + 1 -> positionOffset
                    else -> throw IllegalStateException("Wrong implementation here")
                }
            }
        }
        onTabProgressUpdate?.invoke(tabs, progresses)
    }

    /**
     * Indicator needs to know, according to its progress and sizes of the tabs:
     * (1) Its x position during transition;
     * (2) Its width during transition.
     * Both need to consider the width difference of transitioning tabs.
     */
    private fun evaluateIndicatorProgress(position: Int, positionOffset: Float) {
        if (positionOffset == 0f) { //Which means it reaches the end, special case
            indicator?.x = tabs[position].x
            updateIndicatorWidth(tabs[position].width)
        } else { //Still scrolling
            val lhsTab = tabs[position]
            val rhsTab = tabs[position+1]
            indicator?.x = lhsTab.x + (rhsTab.x - lhsTab.x) * positionOffset
            val newWidth = lhsTab.width + (rhsTab.width - lhsTab.width) * positionOffset
            updateIndicatorWidth(newWidth.toInt())
        }
    }

    private fun updateIndicatorWidth(newWidth: Int) {
        indicator?.layoutParams?.width = newWidth
        indicator?.requestLayout()
    }

    private inline fun <T : View> T.afterMeasured(crossinline onMeasured: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    onMeasured()
                }
            }
        })
    }

}