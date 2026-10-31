package com.challengefriends.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val black = Color.rgb(9, 9, 9)
    private val panel = Color.rgb(25, 25, 25)
    private val gold = Color.rgb(212, 175, 55)
    private val cream = Color.rgb(245, 239, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = black
        window.navigationBarColor = black
        showHome()
    }

    private fun showHome() {
        val page = createPage()
        page.addView(makeText("تحدي الأصدقاء", 30f, gold, true).apply {
            setPadding(0, dp(34), 0, dp(8))
        })
        page.addView(makeText("اللعب يجمعنا... والتحدي يبدأ هنا", 15f, cream, false))
        page.addView(makeText("اختر لعبتك", 20f, gold, true).apply {
            setPadding(0, dp(34), 0, dp(14))
        })

        addGameButton(page, "🧩  ترتيب الصور", "ترتيب القطع لتكوين الصورة")
        addGameButton(page, "❓  تحدي الأسئلة", "أسئلة متنوعة ومنافسة بالنقاط")
        addGameButton(page, "💡  من أنا؟", "اكتشف الكلمة من خلال التلميحات")

        page.addView(makeButton("⚙  الإعدادات", false) { showSettings() }.apply {
            setMargins(top = 24)
        })
        page.addView(makeText("اللعب الجماعي المحلي قيد التجهيز", 12f, Color.LTGRAY, false).apply {
            setPadding(0, dp(28), 0, dp(20))
        })
        setPage(page)
    }

    private fun addGameButton(parent: LinearLayout, title: String, subtitle: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(panel, gold, 1)
            setPadding(dp(18), dp(17), dp(18), dp(17))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        card.addView(makeText(title, 19f, gold, true))
        card.addView(makeText(subtitle, 13f, cream, false).apply {
            setPadding(0, dp(7), 0, 0)
        })
        card.setOnClickListener {
            Toast.makeText(this, "سيتم تنفيذ هذه اللعبة في مرحلتها المخصصة", Toast.LENGTH_SHORT).show()
        }
        parent.addView(card, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, dp(13)) })
    }

    private fun showSettings() {
        val page = createPage()
        page.addView(makeText("الإعدادات", 27f, gold, true).apply {
            setPadding(0, dp(28), 0, dp(24))
        })
        page.addView(makeText("تحدي الأصدقاء", 18f, cream, true))
        page.addView(makeText("الإصدار 1.0", 14f, Color.LTGRAY, false).apply {
            setPadding(0, dp(8), 0, dp(22))
        })
        page.addView(makeText("سيتم إضافة خيارات الصوت والاهتزاز وأسماء اللاعبين في مراحل التطوير التالية.", 15f, cream, false))
        page.addView(makeButton("العودة إلى الرئيسية", true) { showHome() }.apply {
            setMargins(top = 30)
        })
        setPage(page)
    }

    private fun createPage(): LinearLayout {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(black)
            isFillViewport = true
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(22), dp(18), dp(22), dp(24))
        }
        scroll.addView(content)
        currentScroll = scroll
        return content
    }

    private var currentScroll: ScrollView? = null

    private fun setPage(content: LinearLayout) {
        currentScroll?.let { setContentView(it) }
    }

    private fun makeText(value: String, size: Float, color: Int, bold: Boolean): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun makeButton(label: String, filled: Boolean, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 15f
            isAllCaps = false
            setTextColor(if (filled) black else gold)
            background = rounded(if (filled) gold else panel, gold, 1)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setOnClickListener { action() }
        }
    }

    private fun rounded(color: Int, stroke: Int, width: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(16).toFloat()
            setStroke(dp(width), stroke)
        }
    }

    private fun Button.setMargins(top: Int = 0) {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(top) }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
