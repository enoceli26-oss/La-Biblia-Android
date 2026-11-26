package com.biblia.rv1909

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale

class SettingsActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var vocesFiltradas: List<Voice> = emptyList()
    private lateinit var spinnerVoces: Spinner
    private var isEn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val prefs = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE)
        isEn = prefs.getString("app_lang", "es") == "en"

        val btnVolver = findViewById<Button>(R.id.btnVolver)
        btnVolver.text = if (isEn) "← Back" else "← Volver"
        btnVolver.setOnClickListener { finish() }

        val spinnerVersion = findViewById<Spinner>(R.id.spinnerVersion)
        val parentLayout = spinnerVersion.parent as? ViewGroup

        // Selector dinamico de Idioma (Español / English) justo encima de Version
        if (parentLayout != null) {
            val idxVer = parentLayout.indexOfChild(spinnerVersion)
            val tvLang = TextView(this).apply {
                text = if (isEn) "App Language / Idioma:" else "Idioma de la App / Language:"
                setPadding(0, 12, 0, 4)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            val spLang = Spinner(this).apply {
                minimumHeight = 48
                adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Español", "English"))
                setSelection(if (isEn) 1 else 0, false)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        val newLang = if (pos == 1) "en" else "es"
                        if (newLang != prefs.getString("app_lang", "es")) {
                            prefs.edit().putString("app_lang", newLang).apply()
                            recreate()
                        }
                    }
                    override fun onNothingSelected(p: AdapterView<*>?) {}
                }
            }
            parentLayout.addView(tvLang, idxVer.coerceAtLeast(0))
            parentLayout.addView(spLang, (idxVer + 1).coerceAtLeast(1))
        }

        val versiones = listOf(
            "Reina-Valera 1909 (RV1909 - Español)",
            "Reina-Valera 1960 (RV1960 - Español)",
            "King James Version (KJV - English)",
            "World English Bible (WEB - English)"
        )
        val codes = listOf("RV1909", "RV1960", "KJV", "WEB")
        spinnerVersion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, versiones)
        val currentVer = prefs.getString("bible_version", "RV1909") ?: "RV1909"
        spinnerVersion.setSelection(codes.indexOf(currentVer).coerceAtLeast(0), false)
        spinnerVersion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val chosen = codes.getOrElse(pos) { "RV1909" }
                prefs.edit().putString("bible_version", chosen).apply()
                cargarVocesTTS()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        val tvTamano = findViewById<TextView>(R.id.tvTamanoLabel)
        val tvEjemplo = findViewById<TextView>(R.id.tvEjemploLetra)
        val seekTamano = findViewById<SeekBar>(R.id.seekTamano)
        val savedSize = prefs.getFloat("font_size", 18f).toInt()
        seekTamano.progress = (savedSize - 14).coerceIn(0, 16)
        tvTamano.text = if (isEn) "Font size: $savedSize sp" else "Tamaño de letra: $savedSize sp"
        tvEjemplo.text = if (isEn) "In the beginning God created the heaven and the earth." else "En el principio creó Dios los cielos y la tierra."
        tvEjemplo.textSize = savedSize.toFloat()
        seekTamano.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val size = 14 + p
                tvTamano.text = if (isEn) "Font size: $size sp" else "Tamaño de letra: $size sp"
                tvEjemplo.textSize = size.toFloat()
                prefs.edit().putFloat("font_size", size.toFloat()).apply()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        val switchOscuro = findViewById<SwitchMaterial>(R.id.switchModoOscuro)
        switchOscuro.text = if (isEn) "Dark Mode" else "Modo Oscuro"
        switchOscuro.isChecked = prefs.getBoolean("dark_mode", true)
        switchOscuro.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }

        val tvVel = findViewById<TextView>(R.id.tvVelocidadLabel)
        val seekVel = findViewById<SeekBar>(R.id.seekVelocidad)
        val savedRate = prefs.getFloat("speech_rate", 1.0f)
        seekVel.progress = ((savedRate - 0.5f) * 10).toInt().coerceIn(0, 15)
        tvVel.text = String.format(Locale.US, if (isEn) "Reading speed: %.1fx" else "Velocidad de lectura: %.1fx", savedRate)
        seekVel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val rate = 0.5f + (p * 0.1f)
                tvVel.text = String.format(Locale.US, if (isEn) "Reading speed: %.1fx" else "Velocidad de lectura: %.1fx", rate)
                prefs.edit().putFloat("speech_rate", rate).apply()
                tts?.setSpeechRate(rate)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        val tvTono = findViewById<TextView>(R.id.tvTonoLabel)
        val seekTono = findViewById<SeekBar>(R.id.seekTono)
        val savedPitch = prefs.getFloat("speech_pitch", 1.0f)
        seekTono.progress = ((savedPitch - 0.5f) * 10).toInt().coerceIn(0, 10)
        tvTono.text = String.format(Locale.US, if (isEn) "Voice pitch: %.1f" else "Tono de voz: %.1f", savedPitch)
        seekTono.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val pitch = 0.5f + (p * 0.1f)
                tvTono.text = String.format(Locale.US, if (isEn) "Voice pitch: %.1f" else "Tono de voz: %.1f", pitch)
                prefs.edit().putFloat("speech_pitch", pitch).apply()
                tts?.setPitch(pitch)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        spinnerVoces = findViewById(R.id.spinnerVoces)
        val btnProbar = findViewById<Button>(R.id.btnProbarVoz)
        btnProbar.text = if (isEn) "Test Voice" else "Probar Voz"
        btnProbar.setOnClickListener {
            val ver = prefs.getString("bible_version", "RV1909") ?: "RV1909"
            val isEnVer = BibleData.isEnglishVersion(ver)
            tts?.language = if (isEnVer) Locale.US else Locale("es", "ES")
            val sample = if (isEnVer) "In the beginning God created the heaven and the earth." else "En el principio creó Dios los cielos y la tierra."
            tts?.speak(sample, TextToSpeech.QUEUE_FLUSH, null, "test")
        }
        val btnMas = findViewById<Button>(R.id.btnMasVoces)
        btnMas.text = if (isEn) "More Voices" else "Más Voces"
        btnMas.setOnClickListener {
            try { startActivity(Intent("com.android.settings.TTS_SETTINGS")) }
            catch (e: Exception) { Toast.makeText(this, "Settings > Text-to-speech", Toast.LENGTH_SHORT).show() }
        }
        tts = TextToSpeech(this, this)
    }

    private fun cargarVocesTTS() {
        val prefs = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE)
        val ver = prefs.getString("bible_version", "RV1909") ?: "RV1909"
        val targetLang = if (BibleData.isEnglishVersion(ver)) "en" else "es"
        tts?.language = if (targetLang == "en") Locale.US else Locale("es", "ES")
        vocesFiltradas = (tts?.voices ?: emptySet()).filter { it.locale.language == targetLang && (targetLang != "en" || it.locale.country.equals("US", ignoreCase = true)) }
            .sortedWith(compareBy({ it.isNetworkConnectionRequired }, { it.locale.country }, { it.name }))
        val etiquetas = if (vocesFiltradas.isEmpty()) listOf(if (targetLang == "en") "Default Voice (English)" else "Voz predeterminada (Español)") else
            vocesFiltradas.mapIndexed { i, v -> "${if (targetLang == "en") "Voice" else "Voz"} ${i+1} - ${v.locale.displayCountry} [${v.name.takeLast(10)}]" }
        spinnerVoces.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, etiquetas)
        val savedIdx = vocesFiltradas.indexOfFirst { it.name == prefs.getString("voice_name_$targetLang", null) }
        if (savedIdx >= 0) { spinnerVoces.setSelection(savedIdx, false); tts?.voice = vocesFiltradas[savedIdx] }
        spinnerVoces.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos in vocesFiltradas.indices) {
                    tts?.voice = vocesFiltradas[pos]

                    prefs.edit().putString("voice_name_$targetLang", vocesFiltradas[pos].name).apply()
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) cargarVocesTTS() }
    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }
}
        val savedName = prefs.getString("voice_name_$targetLang", null)
        val initIdx = vocesFiltradas.indexOfFirst { it.name == savedName }.coerceAtLeast(0)
        if (initIdx in vocesFiltradas.indices) {
            spinnerVoces.setSelection(initIdx, false)
            tts?.voice = vocesFiltradas[initIdx]
            prefs.edit().putString("voice_name_$targetLang", vocesFiltradas[initIdx].name).commit()
        }
        spinnerVoces.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos in vocesFiltradas.indices) {
                    tts?.voice = vocesFiltradas[pos]
                    prefs.edit().putString("voice_name_$targetLang", vocesFiltradas[pos].name).commit()
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) cargarVocesTTS() }
    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }
}
