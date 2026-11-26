package com.biblia.rv1909

import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject

data class VersiculoFavorito(
    val id: String,
    val version: String,
    val libroIdx: Int,
    val capIdx: Int,
    val verIdx: Int,
    val cita: String,
    val texto: String
)

object FavoritesManager {
    private const val PREFS = "biblia_prefs"
    private const val KEY = "favoritos_json"

    fun generarId(version: String, l: Int, c: Int, v: Int) = "$version:$l:$c:$v"

    fun obtenerTodos(context: Context): MutableList<VersiculoFavorito> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val lista = mutableListOf<VersiculoFavorito>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            lista.add(
                VersiculoFavorito(
                    o.getString("id"),
                    o.optString("version", "RV1909"),
                    o.getInt("libroIdx"),
                    o.getInt("capIdx"),
                    o.getInt("verIdx"),
                    o.getString("cita"),
                    o.getString("texto")
                )
            )
        }
        return lista
    }

    private fun guardarLista(context: Context, lista: List<VersiculoFavorito>) {
        val arr = JSONArray()
        for (f in lista) {
            arr.put(JSONObject().apply {
                put("id", f.id)
                put("version", f.version)
                put("libroIdx", f.libroIdx)
                put("capIdx", f.capIdx)
                put("verIdx", f.verIdx)
                put("cita", f.cita)
                put("texto", f.texto)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }

    fun esFavorito(context: Context, id: String): Boolean = obtenerTodos(context).any { it.id == id }

    fun alternarFavorito(context: Context, item: VersiculoFavorito): Boolean {
        val lista = obtenerTodos(context)
        val idx = lista.indexOfFirst { it.id == item.id }
        return if (idx >= 0) {
            lista.removeAt(idx)
            guardarLista(context, lista)
            false
        } else {
            lista.add(0, item)
            guardarLista(context, lista)
            true
        }
    }

    fun compartirCita(context: Context, cita: String, version: String, texto: String) {
        val mensaje = "«$texto»\n— $cita ($version)"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$cita ($version)")
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir versículo"))
    }
}
