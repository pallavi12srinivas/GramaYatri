package com.gramayatri.app.ui.profile

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramayatri.app.data.GramaYatriStore

class PassengerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Passenger Profile"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }
        val nameInput = EditText(this).apply {
            hint = "Passenger name"
            setText(GramaYatriStore.passengerName(this@PassengerActivity))
            minHeight = 56
        }

        root.addView(titleText("Who is reporting?"))
        root.addView(bodyText("This name appears as the source for bus pings and alerts."))
        root.addView(nameInput)
        root.addView(Button(this).apply {
            text = "Save passenger name"
            isAllCaps = false
            setOnClickListener {
                GramaYatriStore.savePassengerName(this@PassengerActivity, nameInput.text.toString())
                Toast.makeText(this@PassengerActivity, "Passenger saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        })
        setContentView(root)
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