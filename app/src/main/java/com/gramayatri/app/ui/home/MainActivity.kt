package com.gramayatri.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gramayatri.app.data.GramaYatriStore
import com.gramayatri.app.ui.alerts.AlertsActivity
import com.gramayatri.app.ui.profile.PassengerActivity
import com.gramayatri.app.ui.routes.AddRouteActivity
import com.gramayatri.app.ui.routes.RouteDetailActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Grama-Yatri"
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
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)

        root.addView(titleText("Grama-Yatri"))
        root.addView(bodyText("Passenger: ${GramaYatriStore.passengerName(this)}"))
        root.addView(bodyText("Community-powered village bus tracker"))

        root.addView(actionButton("Set passenger name") {
            startActivity(Intent(this, PassengerActivity::class.java))
        })
        root.addView(actionButton("Add new bus and route") {
            startActivity(Intent(this, AddRouteActivity::class.java))
        })
        root.addView(actionButton("Alerts and report history") {
            startActivity(Intent(this, AlertsActivity::class.java))
        })

        root.addView(sectionText("Available routes"))
        GramaYatriStore.routes(this).forEach { route ->
            val latest = GramaYatriStore.latestPing(this, route.id)
            val status = if (route.cancelled) "Cancelled" else latest?.let { "Last: ${it.stopName}, by ${it.reporterName}" } ?: "Waiting for ping"
            root.addView(actionButton("${route.name}\n${route.busNumber} - $status") {
                startActivity(Intent(this, RouteDetailActivity::class.java).putExtra(RouteDetailActivity.EXTRA_ROUTE_ID, route.id))
            })
        }
    }

    private fun titleText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 30f
        setTextColor(0xFF12351F.toInt())
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 4, 0, 8)
    }

    private fun sectionText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTextColor(0xFF173B24.toInt())
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 24, 0, 8)
    }

    private fun bodyText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(0xFF52645A.toInt())
        setPadding(0, 2, 0, 8)
    }

    private fun actionButton(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        setPadding(12, 10, 12, 10)
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 8, 0, 8) }
    }
}