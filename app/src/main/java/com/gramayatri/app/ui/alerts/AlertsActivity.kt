package com.gramayatri.app.ui.alerts

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gramayatri.app.data.GramaYatriStore

class AlertsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Alerts"
        render()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }
        root.addView(titleText("Alerts and report history"))
        val alerts = GramaYatriStore.alerts(this)
        if (alerts.isEmpty()) {
            root.addView(bodyText("No alerts yet."))
        } else {
            alerts.forEach {
                root.addView(cardText("${it.title}\n${it.routeName}\n${it.message}\nSource: Reported by ${it.reporterName}"))
            }
        }
        root.addView(sectionText("Latest passenger pings"))
        GramaYatriStore.pings(this).take(12).forEach {
            root.addView(bodyText("${it.stopName} - ${it.type} - Reported by ${it.reporterName}"))
        }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun titleText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 24f
        setTextColor(0xFF12351F.toInt())
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun sectionText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTextColor(0xFF173B24.toInt())
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 22, 0, 6)
    }

    private fun bodyText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(0xFF3A4D42.toInt())
        setPadding(0, 5, 0, 5)
    }

    private fun cardText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(0xFF4E3A00.toInt())
        setPadding(14, 14, 14, 14)
        setBackgroundColor(0xFFFFF4D8.toInt())
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 10, 0, 10) }
    }
}