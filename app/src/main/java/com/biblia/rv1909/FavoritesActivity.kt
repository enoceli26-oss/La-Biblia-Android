package com.biblia.rv1909

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var tvVacio: TextView
    private var favoritos = mutableListOf<VersiculoFavorito>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        findViewById<Button>(R.id.btnVolverFav).setOnClickListener { finish() }
        tvVacio = findViewById(R.id.tvFavVacio)
        recycler = findViewById(R.id.recyclerFavoritos)
        recycler.layoutManager = LinearLayoutManager(this)
        cargarLista()
    }

    private fun cargarLista() {
        favoritos = FavoritesManager.obtenerTodos(this)
        tvVacio.visibility = if (favoritos.isEmpty()) View.VISIBLE else View.GONE
        recycler.adapter = FavAdapter()
    }

    inner class FavAdapter : RecyclerView.Adapter<FavAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tv: TextView = v.findViewById(R.id.tvTextoVersiculo)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_versiculo, parent, false))
        override fun getItemCount() = favoritos.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = favoritos[position]
            holder.tv.textSize = 16f
            holder.tv.text = "★ ${item.cita} (${item.version})\n${item.texto}"

            holder.itemView.setOnClickListener {
                val data = Intent().apply {
                    putExtra("libroIdx", item.libroIdx)
                    putExtra("capIdx", item.capIdx)
                    putExtra("verIdx", item.verIdx)
                    putExtra("version", item.version)
                }
                setResult(Activity.RESULT_OK, data)
                finish()
            }

            holder.itemView.setOnLongClickListener {
                AlertDialog.Builder(this@FavoritesActivity)
                    .setTitle("${item.cita} (${item.version})")
                    .setItems(arrayOf("📤 Compartir cita", "🗑️ Quitar de Favoritos")) { _, which ->
                        if (which == 0) {
                            FavoritesManager.compartirCita(this@FavoritesActivity, item.cita, item.version, item.texto)
                        } else {
                            FavoritesManager.alternarFavorito(this@FavoritesActivity, item)
                            Toast.makeText(this@FavoritesActivity, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                            cargarLista()
                        }
                    }.show()
                true
            }
        }
    }
}
