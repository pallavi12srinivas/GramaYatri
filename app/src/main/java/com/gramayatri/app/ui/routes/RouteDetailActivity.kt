package com.gramayatri.app.ui.routes

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramayatri.app.data.GramaYatriStore

class RouteDetailActivity : AppCompatActivity() {
    companion object { const val EXTRA_ROUTE_ID = "route_id" }

    private lateinit var routeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routeId = intent.getStringExtra(EXTRA_ROUTE_ID) ?: ""
        title = "Live Route"
        render()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val route = GramaYatriStore.route(this, routeId) ?: return finish()
        val latest = GramaYatriStore.latestPing(this, route.id)
        val latestStop = latest?.stopIndex ?: 0
        val eta = if (route.cancelled) 0 else GramaYatriStore.etaMinutes(route, latestStop)
        val passenger = GramaYatriStore.passengerName(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }

        root.addView(titleText(route.name))
        root.addView(bodyText("Bus: ${route.busNumber}"))
        root.addView(statusText(if (route.cancelled) "ALERT: Morning bus cancelled today" else "Live ETA to ${route.villageTo}: ~$eta min"))
        root.addView(bodyText(latest?.let { "Current tracker: ${it.reporterName} - ${label(it.type)} at ${it.stopName}" } ?: "Current tracker: no passenger has started tracking yet"))

        root.addView(sectionText("Smooth tracking"))
        root.addView(TrackingTimelineView(this).apply {
            setPadding(0, 8, 0, 8)
            setTracking(route, latest, route.cancelled)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 6, 0, 12) }
        })

        root.addView(sectionText("Passenger report"))
        root.addView(actionButton("Start tracking as $passenger") {
            GramaYatriStore.addPing(this, route.id, latestStop, "ON_BUS", passenger)
            Toast.makeText(this, "$passenger is now tracking this bus", Toast.LENGTH_SHORT).show()
            render()
        })
        root.addView(actionButton("Move to next stop: bus passed me") {
            val next = ((GramaYatriStore.latestPing(this, route.id)?.stopIndex ?: -1) + 1).coerceAtMost(route.stops.lastIndex)
            GramaYatriStore.addPing(this, route.id, next, "BUS_PASSED", passenger)
            Toast.makeText(this, "Ping sent", Toast.LENGTH_SHORT).show()
            render()
        })
        root.addView(actionButton("Simulate another passenger ping") {
            val names = listOf("Ravi", "Asha", "Meena", "Kiran", "Sahana")
            val current = GramaYatriStore.latestPing(this, route.id)?.stopIndex ?: 0
            val next = (current + 1).coerceAtMost(route.stops.lastIndex)
            GramaYatriStore.addPing(this, route.id, next, "BUS_PASSED", names[(System.currentTimeMillis() % names.size).toInt()])
            render()
        })
        root.addView(actionButton("Report delay") {
            GramaYatriStore.addAlert(this, route.id, "Delay reported", "Passengers reported a delay on this route.", "DELAY", passenger)
            Toast.makeText(this, "Delay alert added", Toast.LENGTH_SHORT).show()
            render()
        })
        root.addView(actionButton("Report morning bus cancelled") {
            GramaYatriStore.addAlert(this, route.id, "Bus cancelled", "The morning bus is cancelled today.", "CANCELLED", passenger)
            Toast.makeText(this, "Cancellation alert added", Toast.LENGTH_SHORT).show()
            render()
        })

        root.addView(sectionText("Recent reports"))
        GramaYatriStore.pings(this, route.id).take(6).forEach {
            root.addView(bodyText("${label(it.type)} - ${it.stopName} - Reported by ${it.reporterName}"))
        }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun label(type: String) = when (type) {
        "ON_BUS" -> "I am on bus"
        "BUS_PASSED" -> "Bus passed me"
        else -> type
    }

    private fun titleText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 25f
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

    private fun statusText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 20f
        setTextColor(0xFF173B24.toInt())
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(12, 14, 12, 14)
        setBackgroundColor(0xFFE7F3E7.toInt())
    }

    private fun bodyText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(0xFF3A4D42.toInt())
        setPadding(0, 5, 0, 5)
    }

    private fun actionButton(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 7, 0, 7) }
    }
}