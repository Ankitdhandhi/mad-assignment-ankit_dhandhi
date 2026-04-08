package com.example.currencyx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etAmount;
    private Spinner spinnerFrom, spinnerTo;
    private MaterialButton btnConvert, btnSwap;
    private ImageButton btnSettings;
    private MaterialCardView cardResult;
    private TextView tvResult, tvRate;
    private LinearLayout llAllRates;
    private SharedPreferences prefs;

    private List<String> currencies = Arrays.asList("INR 🇮🇳", "USD 🇺🇸", "JPY 🇯🇵", "EUR 🇪🇺");
    private List<String> currencyCodes = Arrays.asList("INR", "USD", "JPY", "EUR");

    private Map<String, String> currencySymbols = new HashMap<>();
    private Map<String, Double> ratesFromUSD = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Maps
        currencySymbols.put("INR", "₹");
        currencySymbols.put("USD", "$");
        currencySymbols.put("JPY", "¥");
        currencySymbols.put("EUR", "€");

        ratesFromUSD.put("INR", 83.5);
        ratesFromUSD.put("USD", 1.0);
        ratesFromUSD.put("JPY", 151.2);
        ratesFromUSD.put("EUR", 0.92);

        etAmount = findViewById(R.id.etAmount);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        btnConvert = findViewById(R.id.btnConvert);
        btnSwap = findViewById(R.id.btnSwap);
        btnSettings = findViewById(R.id.btnSettings);
        cardResult = findViewById(R.id.cardResult);
        tvResult = findViewById(R.id.tvResult);
        tvRate = findViewById(R.id.tvRate);
        llAllRates = findViewById(R.id.llAllRates);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
        spinnerTo.setSelection(1);

        btnConvert.setOnClickListener(v -> convert());
        btnSwap.setOnClickListener(v -> swap());

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class))
        );

        populateAllRates();
    }

    private void convert() {
        String input = etAmount.getText().toString();

        if (input.isEmpty()) {
            etAmount.setError("Enter an amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(input);
        } catch (Exception e) {
            etAmount.setError("Invalid number");
            return;
        }

        String fromCode = currencyCodes.get(spinnerFrom.getSelectedItemPosition());
        String toCode = currencyCodes.get(spinnerTo.getSelectedItemPosition());

        double result = convertAmount(amount, fromCode, toCode);
        String symbol = currencySymbols.containsKey(toCode) ? currencySymbols.get(toCode) : "";

        tvResult.setText(symbol + " " + String.format("%.4f", result));
        tvRate.setText("1 " + fromCode + " = " +
                String.format("%.4f", convertAmount(1.0, fromCode, toCode)) + " " + toCode);

        cardResult.setVisibility(View.VISIBLE);
    }

    private double convertAmount(double amount, String from, String to) {
        double inUSD = amount / (ratesFromUSD.containsKey(from) ? ratesFromUSD.get(from) : 1.0);
        return inUSD * (ratesFromUSD.containsKey(to) ? ratesFromUSD.get(to) : 1.0);
    }

    private void swap() {
        int fromPos = spinnerFrom.getSelectedItemPosition();
        int toPos = spinnerTo.getSelectedItemPosition();

        spinnerFrom.setSelection(toPos);
        spinnerTo.setSelection(fromPos);
    }

    private void populateAllRates() {
        llAllRates.removeAllViews();

        for (String from : currencyCodes) {
            for (String to : currencyCodes) {

                if (from.equals(to)) continue;

                View row = getLayoutInflater()
                        .inflate(android.R.layout.simple_list_item_2, null);

                TextView text1 = row.findViewById(android.R.id.text1);
                TextView text2 = row.findViewById(android.R.id.text2);

                text1.setText(from + " → " + to);
                text2.setText("1 " + from + " = " +
                        String.format("%.4f", convertAmount(1.0, from, to)) + " " + to);

                llAllRates.addView(row);
            }
        }
    }
}