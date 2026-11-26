package com.biblia.rv1909

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale

class SettingsActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var vocesEspanol: List<Voice> = emptyList()
    private lateinit var spinnerVoces: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val prefs = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE)
        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }

        val spinnerVersion = findViewById<Spinner>(R.id.spinnerVersion)
        val versiones = listOf("Reina-Valera 1909 (RV1909)", "Reina-Valera 1960 (RV1960)")
        spinnerVersion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, versiones)
        val currentVer = prefs.getString("bible_version", "RV1909")
        spinnerVersion.setSelection(if (currentVer == "RV1960") 1 else 0, false)
        spinnerVersion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                prefs.edit().putString("bible_version", if (pos == 1) "RV1960" else "RV1909").apply()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        val tvTamano = findViewById<TextView>(R.id.tvTamanoLabel)
        val tvEjemplo = findViewById<TextView>(R.id.tvEjemploLetra)
        val seekTamano = findViewById<SeekBar>(R.id.seekTamano)
        val savedSize = prefs.getFloat("font_size", 18f).toInt()
        seekTamano.progress = (savedSize - 14).coerceIn(0, 16)
        tvTamano.text = "Tamaño de letra: $savedSize sp"
        tvEjemplo.textSize = savedSize.toFloat()
        seekTamano.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val size = 14 + p
                tvTamano.text = "Tamaño de letra: $size sp"
                tvEjemplo.textSize = size.toFloat()
                prefs.edit().putFloat("font_size", size.toFloat()).apply()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        val switchOscuro = findViewById<SwitchMaterial>(R.id.switchModoOscuro)
        switchOscuro.isChecked = prefs.getBoolean("dark_mode", true)
        switchOscuro.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }

        val tvVel = findViewById<TextView>(R.id.tvVelocidadLabel)
        val seekVel = findViewById<SeekBar>(R.id.seekVelocidad)
        val savedRate = prefs.getFloat("speech_rate", 1.0f)
        seekVel.progress = ((savedRate - 0.5f) * 10).toInt().coerceIn(0, 15)
        tvVel.text = String.format(Locale.US, "Velocidad de lectura: %.1fx", savedRate)
        seekVel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val rate = 0.5f + (p * 0.1f)
                tvVel.text = String.format(Locale.US, "Velocidad de lectura: %.1fx", rate)
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
        tvTono.text = String.format(Locale.US, "Tono de voz: %.1f", savedPitch)
        seekTono.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val pitch = 0.5f + (p * 0.1f)
                tvTono.text = String.format(Locale.US, "Tono de voz: %.1f", pitch)
                prefs.edit().putFloat("speech_pitch", pitch).apply()
                tts?.setPitch(pitch)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        spinnerVoces = findViewById(R.id.spinnerVoces)
        findViewById<Button>(R.id.btnProbarVoz).setOnClickListener {
            tts?.speak("En el principio creó Dios los cielos y la tierra.", TextToSpeech.QUEUE_FLUSH, null, "test")
        }
        findViewById<Button>(R.id.btnMasVoces).setOnClickListener {
            try { startActivity(Intent("com.android.settings.TTS_SETTINGS")) }
            catch (e: Exception) { Toast.makeText(this, "Ajustes > Texto a voz", Toast.LENGTH_SHORT).show() }
        }
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val prefs = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE)
            vocesEspanol = (tts?.voices ?: emptySet()).filter { it.locale.language == "es" }
                .sortedWith(compareBy({ it.isNetworkConnectionRequired }, { it.locale.country }, { it.name }))
            val etiquetas = if (vocesEspanol.isEmpty()) listOf("Voz predeterminada (Español)") else
                vocesEspanol.mapIndexed { i, v -> "Voz ${i+1} - ${v.locale.displayCountry} [${v.name.takeLast(10)}]" }
            spinnerVoces.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, etiquetas)
            val savedIdx = vocesEspanol.indexOfFirst { it.name == prefs.getString("voice_name", null) }
            if (savedIdx >= 0) { spinnerVoces.setSelection(savedIdx, false); tts?.voice = vocesEspanol[savedIdx] }
            spinnerVoces.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    if (pos in vocesEspanol.indices) {
                        tts?.voice = vocesEspanol[pos]
                        prefs.edit().putString("voice_name", vocesEspanol[pos].name).apply()
                    }
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }
    }

    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }
}
