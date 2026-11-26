package com.biblia.rv1909

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var spinnerLibro: Spinner
    private lateinit var spinnerCapitulo: Spinner
    private lateinit var recycler: RecyclerView
    private lateinit var btnPlayPause: Button
    private lateinit var btnAnterior: Button
    private lateinit var btnSiguiente: Button
    private lateinit var tvEstado: TextView

    private lateinit var libros: List<Libro>
    private var versionActual = "RV1909"
    private var libroIdx = 0
    private var capIdx = 0
    private var versiculoActual = 0
    private var reproduciendo = false
    private var ttsListo = false
    private var tts: TextToSpeech? = null
    private lateinit var adapter: VersiculoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.getBoolean("dark_mode", true)) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerLibro = findViewById(R.id.spinnerLibro)
        spinnerCapitulo = findViewById(R.id.spinnerCapitulo)
        recycler = findViewById(R.id.recyclerVersiculos)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnAnterior = findViewById(R.id.btnAnterior)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        tvEstado = findViewById(R.id.tvEstadoLectura)

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            pausarLectura()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnConcordancia).setOnClickListener {
            pausarLectura()
            @Suppress("DEPRECATION")
            startActivityForResult(Intent(this, ConcordanceActivity::class.java), 100)
        }
        findViewById<ImageButton>(R.id.btnFavoritos).setOnClickListener {
            pausarLectura()
            @Suppress("DEPRECATION")
            startActivityForResult(Intent(this, FavoritesActivity::class.java), 101)
        }

        versionActual = prefs.getString("bible_version", "RV1909") ?: "RV1909"
        libros = BibleData.obtenerLibros(this, versionActual)
        libroIdx = prefs.getInt("last_book", 0).coerceIn(0, libros.lastIndex)
        capIdx = prefs.getInt("last_chapter", 0).coerceIn(0, libros[libroIdx].capitulos.lastIndex)

        adapter = VersiculoAdapter(emptyList()) { pos -> versiculoActual = pos; iniciarLectura() }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        configurarSpinners()

        btnPlayPause.setOnClickListener { if (reproduciendo) pausarLectura() else iniciarLectura() }
        btnAnterior.setOnClickListener {
            if (versiculoActual > 0) {
                versiculoActual--
                if (reproduciendo) hablarVersiculoActual() else actualizarSeleccion()
            } else if (capIdx > 0) spinnerCapitulo.setSelection(capIdx - 1)
        }
        btnSiguiente.setOnClickListener {
            val total = libros[libroIdx].capitulos[capIdx].size
            if (versiculoActual + 1 < total) {
                versiculoActual++
                if (reproduciendo) hablarVersiculoActual() else actualizarSeleccion()
            } else if (capIdx + 1 < libros[libroIdx].capitulos.size) spinnerCapitulo.setSelection(capIdx + 1)
        }
        tts = TextToSpeech(this, this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == 100 || requestCode == 101) && resultCode == Activity.RESULT_OK && data != null) {
            data.getStringExtra("version")?.let { ver ->
                if (ver != versionActual) {
                    versionActual = ver
                    getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE).edit().putString("bible_version", versionActual).apply()
                    libros = BibleData.obtenerLibros(this, versionActual)
                }
            }
            libroIdx = data.getIntExtra("libroIdx", 0).coerceIn(0, libros.lastIndex)
            capIdx = data.getIntExtra("capIdx", 0).coerceIn(0, libros[libroIdx].capitulos.lastIndex)
            versiculoActual = data.getIntExtra("verIdx", 0).coerceIn(0, libros[libroIdx].capitulos[capIdx].lastIndex)
            spinnerLibro.setSelection(libroIdx, false)
            actualizarSpinnerCapitulos(capIdx)
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE)
        val ver = prefs.getString("bible_version", "RV1909") ?: "RV1909"
        versionActual = ver
        libros = BibleData.obtenerLibros(this, versionActual)
        val prevBook = libroIdx.coerceIn(0, libros.size - 1)
        val prevCap = capIdx.coerceIn(0, libros[prevBook].capitulos.size - 1)
        libroIdx = prevBook
        capIdx = prevCap
        spinnerLibro.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, libros.map { it.nombre })
        spinnerLibro.setSelection(prevBook, false)
        actualizarSpinnerCapitulos(prevCap)
        adapter.actualizarDatos(libros[libroIdx].capitulos[capIdx], versiculoActual)
        tvEstado.text = "[$versionActual] ${libros[libroIdx].nombre} ${capIdx + 1}:${versiculoActual + 1}"
        if (ttsListo) aplicarConfiguracionTTS()
    }

    private fun configurarSpinners() {
        spinnerLibro.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, libros.map { it.nombre })
        spinnerLibro.setSelection(libroIdx.coerceIn(0, libros.size - 1), false)
        spinnerLibro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos != libroIdx) { libroIdx = pos; capIdx = 0; versiculoActual = 0; actualizarSpinnerCapitulos(0) }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        spinnerCapitulo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos != capIdx) { capIdx = pos; versiculoActual = 0; cargarCapitulo() }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        actualizarSpinnerCapitulos(capIdx)
    }

    private fun actualizarSpinnerCapitulos(seleccion: Int) {
        val prefs = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE)
        val isEn = prefs.getString("app_lang", "es") == "en"
        val prefix = if (isEn) "Ch." else "Cap."
        val numCaps = libros[libroIdx].capitulos.size
        spinnerCapitulo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (1..numCaps).map { "$prefix $it" })
        spinnerCapitulo.setSelection(seleccion.coerceIn(0, numCaps - 1), false)
        capIdx = seleccion.coerceIn(0, numCaps - 1)
        cargarCapitulo()
    }

    private fun cargarCapitulo() {
        val vers = libros[libroIdx].capitulos[capIdx]
        versiculoActual = versiculoActual.coerceIn(0, (vers.size - 1).coerceAtLeast(0))
        adapter.actualizarDatos(vers, versiculoActual)
        recycler.scrollToPosition(versiculoActual)
        tvEstado.text = "[$versionActual] ${libros[libroIdx].nombre} ${capIdx + 1}:${versiculoActual + 1}"
    }

    private fun actualizarSeleccion() {
        adapter.setVersiculoActivo(versiculoActual)
        recycler.smoothScrollToPosition(versiculoActual)
        tvEstado.text = "[$versionActual] ${libros[libroIdx].nombre} ${capIdx + 1}:${versiculoActual + 1}"
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
            aplicarConfiguracionTTS()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onError(id: String?) {}
                override fun onDone(id: String?) {
                    runOnUiThread {
                        if (!reproduciendo) return@runOnUiThread
                        val total = libros[libroIdx].capitulos[capIdx].size
                        if (versiculoActual + 1 < total) { versiculoActual++; hablarVersiculoActual() }
                        else if (capIdx + 1 < libros[libroIdx].capitulos.size) spinnerCapitulo.setSelection(capIdx + 1)
                        else if (libroIdx + 1 < libros.size) { libroIdx++; capIdx = 0; versiculoActual = 0; spinnerLibro.setSelection(libroIdx); actualizarSpinnerCapitulos(0); hablarVersiculoActual() }
                        else pausarLectura()
                    }
                }
            })
            ttsListo = true
        }
    }

    private fun aplicarConfiguracionTTS() {
        val prefs = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE)
        val isEnVer = BibleData.isEnglishVersion(versionActual)
        val targetLang = if (isEnVer) "en" else "es"
        tts?.language = if (isEnVer) Locale.US else Locale("es", "ES")
        tts?.setSpeechRate(prefs.getFloat("speech_rate", 1.0f))
        tts?.setPitch(prefs.getFloat("speech_pitch", 1.0f))
        val savedVoice = prefs.getString("voice_name_$targetLang", null) ?: prefs.getString("voice_name", null)
        val voces = (tts?.voices ?: emptySet()).filter { it.locale.language.equals(targetLang, true) && (targetLang != "en" || it.locale.country.equals("US", true)) && (targetLang != "en" || it.locale.country.equals("US", ignoreCase = true)) }
        val match = voces.firstOrNull { it.name == savedVoice } ?: voces.firstOrNull { !it.isNetworkConnectionRequired } ?: voces.firstOrNull()
        if (match != null) tts?.voice = match
    }

    private fun iniciarLectura() {
        if (!ttsListo) { Toast.makeText(this, "Inicializando voz...", Toast.LENGTH_SHORT).show(); return }
        reproduciendo = true; btnPlayPause.text = "⏸ Pausa"; hablarVersiculoActual()
    }

    private fun pausarLectura() {
        reproduciendo = false; tts?.stop(); btnPlayPause.text = "▶ Play"
        tvEstado.text = "[$versionActual] Pausado en ${libros[libroIdx].nombre} ${capIdx + 1}:${versiculoActual + 1}"
    }

    private fun hablarVersiculoActual() {
        val versiculos = libros[libroIdx].capitulos[capIdx]
        if (versiculoActual !in versiculos.indices) return
        actualizarSeleccion()
        tts?.speak(versiculos[versiculoActual], TextToSpeech.QUEUE_FLUSH, Bundle(), "v_$versiculoActual")
    }

    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }

    inner class VersiculoAdapter(private var lista: List<String>, private val onClick: (Int) -> Unit) : RecyclerView.Adapter<VersiculoAdapter.VH>() {
        private var activo = 0
        fun actualizarDatos(nueva: List<String>, idxActivo: Int) { lista = nueva; activo = idxActivo; notifyDataSetChanged() }
        fun setVersiculoActivo(idx: Int) { val prev = activo; activo = idx; notifyItemChanged(prev); notifyItemChanged(activo) }
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val container: LinearLayout = v.findViewById(R.id.containerVersiculo)
            val tv: TextView = v.findViewById(R.id.tvTextoVersiculo)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_versiculo, parent, false))
        override fun getItemCount() = lista.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            val fontSize = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE).getFloat("font_size", 18f)
            val idFav = FavoritesManager.generarId(versionActual, libroIdx, capIdx, position)
            val esFav = FavoritesManager.esFavorito(this@MainActivity, idFav)
            val estrella = if (esFav) "★ " else ""
            holder.tv.textSize = fontSize
            holder.tv.text = "$estrella${position + 1}. ${lista[position]}"
            holder.container.setBackgroundColor(if (position == activo) Color.parseColor("#334A90E2") else Color.TRANSPARENT)

            holder.itemView.setOnClickListener { onClick(position) }
            holder.itemView.setOnLongClickListener {
                val cita = "${libros[libroIdx].nombre} ${capIdx + 1}:${position + 1}"
                val texto = lista[position]
                val opcionFav = if (esFav) "🗑️ Quitar de Favoritos" else "⭐ Guardar en Favoritos"
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("$cita ($versionActual)")
                    .setItems(arrayOf(opcionFav, "📤 Compartir cita")) { _, which ->
                        if (which == 0) {
                            val agregado = FavoritesManager.alternarFavorito(
                                this@MainActivity,
                                VersiculoFavorito(idFav, versionActual, libroIdx, capIdx, position, cita, texto)
                            )
                            Toast.makeText(this@MainActivity, if (agregado) "Guardado en Favoritos" else "Quitado de Favoritos", Toast.LENGTH_SHORT).show()
                            notifyItemChanged(position)
                        } else {
                            FavoritesManager.compartirCita(this@MainActivity, cita, versionActual, texto)
                        }
                    }.show()
                true
            }
        }
    }
}
