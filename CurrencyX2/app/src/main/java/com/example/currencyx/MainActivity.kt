package com.student.currencyx

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var btnConvert: MaterialButton
    private lateinit var btnSwap: MaterialButton
    private lateinit var btnSettings: ImageButton
    private lateinit var cardResult: MaterialCardView
    private lateinit var tvResult: TextView
    private lateinit var tvRate: TextView
    private lateinit var llAllRates: LinearLayout
    private lateinit var prefs: SharedPreferences

    private val currencies = listOf("INR 🇮🇳", "USD 🇺🇸", "JPY 🇯🇵", "EUR 🇪🇺")
    private val currencyCodes = listOf("INR", "USD", "JPY", "EUR")
    private val currencySymbols = mapOf("INR" to "₹", "USD" to "$", "JPY" to "¥", "EUR" to "€")

    // Static rates relative to USD (update for real use)
    private val ratesFromUSD = mapOf(
        "INR" to 83.5,
        "USD" to 1.0,
        "JPY" to 151.2,
        "EUR" to 0.92
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etAmount = findViewById(R.id.etAmount)
        spinnerFrom = findViewById(R.id.spinnerFrom)
        spinnerTo = findViewById(R.id.spinnerTo)
        btnConvert = findViewById(R.id.btnConvert)
        btnSwap = findViewById(R.id.btnSwap)
        btnSettings = findViewById(R.id.btnSettings)
        cardResult = findViewById(R.id.cardResult)
        tvResult = findViewById(R.id.tvResult)
        tvRate = findViewById(R.id.tvRate)
        llAllRates = findViewById(R.id.llAllRates)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter
        spinnerTo.setSelection(1)

        btnConvert.setOnClickListener { convert() }
        btnSwap.setOnClickListener { swap() }
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        populateAllRates()
    }

    private fun convert() {
        val input = etAmount.text.toString()
        if (input.isEmpty()) {
            etAmount.error = "Enter an amount"
            return
        }
        val amount = input.toDoubleOrNull() ?: run {
            etAmount.error = "Invalid number"
            return
        }
        val fromCode = currencyCodes[spinnerFrom.selectedItemPosition]
        val toCode = currencyCodes[spinnerTo.selectedItemPosition]
        val result = convertAmount(amount, fromCode, toCode)
        val symbol = currencySymbols[toCode] ?: ""
        tvResult.text = "$symbol ${"%.4f".format(result)}"
        tvRate.text = "1 $fromCode = ${"%.4f".format(convertAmount(1.0, fromCode, toCode))} $toCode"
        cardResult.visibility = View.VISIBLE
    }

    private fun convertAmount(amount: Double, from: String, to: String): Double {
        val inUSD = amount / (ratesFromUSD[from] ?: 1.0)
        return inUSD * (ratesFromUSD[to] ?: 1.0)
    }

    private fun swap() {
        val fromPos = spinnerFrom.selectedItemPosition
        val toPos = spinnerTo.selectedItemPosition
        spinnerFrom.setSelection(toPos)
        spinnerTo.setSelection(fromPos)
    }

    private fun populateAllRates() {
        llAllRates.removeAllViews()
        for (from in currencyCodes) {
            for (to in currencyCodes) {
                if (from == to) continue
                val row = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
                val text1 = row.findViewById<TextView>(android.R.id.text1)
                val text2 = row.findViewById<TextView>(android.R.id.text2)
                text1.text = "$from → $to"
                text2.text = "1 $from = ${"%.4f".format(convertAmount(1.0, from, to))} $to"
                llAllRates.addView(row)
            }
        }
    }
}