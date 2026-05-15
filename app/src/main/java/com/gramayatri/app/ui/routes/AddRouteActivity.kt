package com.gramayatri.app.ui.routes

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramayatri.app.data.GramaYatriStore

class AddRouteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Add Bus Route"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }
        val routeInput = input("Route name, e.g. Village A - Market")
        val busInput = input("Bus number/name, e.g. KA-12 Village Bus")
        val stopsInput = input("Stops comma separated, e.g. Village A, School Stop, Market")

        root.addView(titleText("Add new bus and route"))
        root.addView(bodyText("Add at least two stops. The app will create average travel times for ETA."))
        root.addView(routeInput)
        root.addView(busInput)
        root.addView(stopsInput)
        root.addView(Button(this).apply {
            text = "Save bus route"
            isAllCaps = false
            setOnClickListener {
                val stops = stopsInput.text.toString().split(',').map { it.trim() }.filter { it.isNotEmpty() }
                if (routeInput.text.isBlank() || busInput.text.isBlank() || stops.size < 2) {
                    Toast.makeText(this@AddRouteActivity, "Enter route, bus, and at least two stops", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                GramaYatriStore.addRoute(this@AddRouteActivity, routeInput.text.toString(), busInput.text.toString(), stops)
                Toast.makeText(this@AddRouteActivity, "Route added", Toast.LENGTH_SHORT).show()
                finish()
            }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        })
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun input(hintText: String) = EditText(this).apply {
        hint = hintText
        minHeight = 56
        setSingleLine(false)
    }

    private fun titleText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 24f
        setTextColor(0xFF12351F.toInt())
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun bodyText(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(0xFF52645A.toInt())
        setPadding(0, 8, 0, 18)
    }
}