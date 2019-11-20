# FlexibleTabLayout

Often we encounter requirements that the built-in `TabLayout` cannot fulfill, even if they are just simple UI or styling requirements.  
This is usually due to the limiting design of TabLayout, for example you cannot customize the indicator stripe, nor the tab cannot get any callback when viewpager is being scrolled.

This `FlexibleTabLayout` tries to allow more room for customization, requiring you to pass in your own custom view; and it provides callback for you to modify your custom views too.

Since this project is in an early stage, it has the below assumptions / limitations:

1. Supports only horizontal tabs
2. Supports only non-scrollable tabs
3. Assumes indicator is below tabs
4. Assumes indicator cannot be longer than it's tab's width.

So, `FlexibleTabLayout` is not something that give you a lot of choices out of the box; instead you would have to write plenty of code.  
But on the other hand, you get more control and flexibility from it.

## Usage

### Add to Project

First make sure `jcenter()` is included as a repository in your **project**'s build.gradle:  

```groovy
allprojects {
    repositories {
        jcenter()
    }
}
```

And then add the below to your app's build.gradle:  

```groovy
    implementation 'com.asksira.android:flexibletablayout:0.1.0'
```

### Step 1: Create FlexibleTabLayout in XML

```xml
    <com.asksira.flexibletablayout.FlexibleTabLayout
        android:id="@+id/flexibleTabLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:has_indicator="true"/>
```

### Step 2: Create your own XML for tab and indicator

`FlexibleTabLayout` does not provide any built-in UI / style for tab or indicator.  
Simply create your own, and inflate them in the later steps.

Please note that indicator's root container's width must be `match_parent` in order for it to work properly.  
(Indicator's root container's width always equals the width of the tab. If you want a smaller indicator, simply create child views under the root element.)

You may refer to the example in the demo app.

### Step 3: Tell FlexibleTabLayout how to inflate your own tab or indicator

`FlexibleTabLayout` has 2 delegates: `inflateTabsDelegate` and `inflateIndicatorDelegate`.  
Both delegate provide you the container, and you will implement the delegate and return a list of `View` for tabs, and one single `View` for indicator.  

```kotlin
        ftl.inflateTabsDelegate = { container ->
            val list: ArrayList<View> = arrayListOf()
            //For each tab you need, inflate a View. Do not attach to the container!
            (1..4).forEach { i -> 
                val tab = LayoutInflater.from(container.context).inflate(R.layout.item_tab, container, false)
                //In between here you will probably want to modify the content of the tab you inflated
                list.add(tab)
            }
            list.toList()
        }
        ftl.inflateIndicatorDelegate = { container ->
            val indicator = LayoutInflater.from(container.context).inflate(R.layout.item_indicator, container, false)
            indicator
        }
```
### Step 4: In ViewPager.OnPageChangeListener, notify FlexibleTabLayout

Note that `FlexibleTabLayout` does not extend `TabLayout`. So you cannot use `viewPager.setupWithTabLayout()`.

Instead:

```kotlin
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
```

### Step 5: Inflate tabs and indicator

```kotlin
        ftl.inflateTabs()
        ftl.inflateIndicator()
```

Now, you will notice that when you scroll / select page in ViewPager, the indicator knows how to position, scale and animate itself.

### Optional: Do anything you like when viewpager is being scrolled

in `onTabProgressUpdate` callback, you will get a list of tabs (`View`s) and a list of progress (`Float`).  
For example,  
If progresses[0] is 0, it means the first tab is not selected.
If progresses[1] is 0.5, it means the 2nd tab is being scrolled to/from its adjacent page, in the progress of 0.5.
If progresses[1] is 1, it means the 2nd tab is selected.

For exmaple in the demo app:
```kotlin
        ftl.onTabProgressUpdate = { tabs, progresses ->
            tabs.forEachIndexed { index, view ->
                view.findViewById<TextView>(R.id.tvTabName).alpha = progresses[index]
            }
        }
```

The effect would be the tab names will change from transparent to fully visible when it is being scrolled to it;
And change from fully visible to transparent when it is being scrolled away.

Actually, the tabs and indicator that you told `FlexibleTabLayout` how to inflate, are both public objects.  
So you can access them any time and modify them as you like.

Common usage are, for example, changing tab name's color or size.

## Release notes

v0.1.0
- First release

## License

```
Copyright 2019 Sira Lam

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
associated documentation files (the FlexibleTabLayout), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or 
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```