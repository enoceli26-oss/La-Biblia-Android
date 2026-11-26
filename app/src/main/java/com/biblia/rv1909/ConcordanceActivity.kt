package com.biblia.rv1909

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.Normalizer

class ConcordanceActivity : AppCompatActivity() {
    private var consultaActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concordance)

        val prefs = getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE)
        val version = prefs.getString("bible_version", "RV1909") ?: "RV1909"

        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        val btnBuscar = findViewById<Button>(R.id.btnEjecutarBuscar)
        val tvCount = findViewById<TextView>(R.id.tvResultadosCount)
        val recycler = findViewById<RecyclerView>(R.id.recyclerResultados)
        recycler.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnVolverConc).setOnClickListener { finish() }

        fun ejecutar() {
            val query = etBuscar.text.toString().trim()
            if (query.length < 2) {
                Toast.makeText(this, "Escribe al menos 2 letras", Toast.LENGTH_SHORT).show()
                return
            }
            consultaActual = query
            val res = BibleData.buscarConcordancia(this, version, query, 500)
            tvCount.text = "Encontrados ${res.size} versículos en $version para \"$query\""
            recycler.adapter = ResultadosAdapter(res) { item ->
                val data = Intent().apply {
                    putExtra("libroIdx", item.libroIdx)
                    putExtra("capIdx", item.capIdx)
                    putExtra("verIdx", item.verIdx)
                }
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }

        btnBuscar.setOnClickListener { ejecutar() }
        etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { ejecutar(); true } else false
        }
    }

    private fun norm(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase()

    inner class ResultadosAdapter(private val lista: List<ResultadoConcordancia>, private val onClick: (ResultadoConcordancia) -> Unit) :
        RecyclerView.Adapter<ResultadosAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tv: TextView = v.findViewById(R.id.tvTextoVersiculo)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_versiculo, parent, false))
        override fun getItemCount() = lista.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = lista[position]
            val completo = "${item.cita} — ${item.texto}"
            val span = SpannableString(completo)
            val qNorm = norm(consultaActual)
            val tNorm = norm(completo)
            var idx = tNorm.indexOf(qNorm)
            while (idx >= 0 && qNorm.isNotEmpty()) {
                val end = (idx + qNorm.length).coerceAtMost(completo.length)
                span.setSpan(BackgroundColorSpan(Color.parseColor("#FFD54F")), idx, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(ForegroundColorSpan(Color.BLACK), idx, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                idx = tNorm.indexOf(qNorm, end)
            }
            holder.tv.textSize = 16f
            holder.tv.text = span
            holder.itemView.setOnClickListener { onClick(item) }
        }
    }
}
