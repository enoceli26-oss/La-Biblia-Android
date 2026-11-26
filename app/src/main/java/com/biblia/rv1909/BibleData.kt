package com.biblia.rv1909

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer

data class Libro(val nombre: String, val capitulos: List<List<String>>)
data class ResultadoConcordancia(val libroIdx: Int, val capIdx: Int, val verIdx: Int, val cita: String, val texto: String)

object BibleData {
    private val cache = mutableMapOf<String, List<Libro>>()

    val nombresLibros = listOf(
        "Génesis", "Éxodo", "Levítico", "Números", "Deuteronomio",
        "Josué", "Jueces", "Rut", "1 Samuel", "2 Samuel",
        "1 Reyes", "2 Reyes", "1 Crónicas", "2 Crónicas", "Esdras",
        "Nehemías", "Ester", "Job", "Salmos", "Proverbios",
        "Eclesiastés", "Cantares", "Isaías", "Jeremías", "Lamentaciones",
        "Ezequiel", "Daniel", "Oseas", "Joel", "Amós",
        "Abdías", "Jonás", "Miqueas", "Nahúm", "Habacuc",
        "Sofonías", "Hageo", "Zacarías", "Malaquías",
        "Mateo", "Marcos", "Lucas", "Juan", "Hechos",
        "Romanos", "1 Corintios", "2 Corintios", "Gálatas", "Efesios",
        "Filipenses", "Colosenses", "1 Tesalonicenses", "2 Tesalonicenses",
        "1 Timoteo", "2 Timoteo", "Tito", "Filemón", "Hebreos",
        "Santiago", "1 Pedro", "2 Pedro", "1 Juan", "2 Juan",
        "3 Juan", "Judas", "Apocalipsis"
    )

    private fun normalizar(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase().trim()

    fun obtenerLibros(context: Context, version: String = "RV1909"): List<Libro> {
        cache[version]?.let { return it }
        val lista = if (version == "RV1960") cargarRV1960(context) else cargarRV1909(context)
        cache[version] = lista
        return lista
    }

    private fun cargarRV1909(context: Context): List<Libro> {
        val raw = context.assets.open("biblia_rv1909.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val jsonArray = JSONArray(raw.removePrefix("\uFEFF"))
        val lista = mutableListOf<Libro>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val nombre = if (i < nombresLibros.size) nombresLibros[i] else "Libro ${i+1}"
            val capsJson = obj.getJSONArray("chapters")
            val caps = mutableListOf<List<String>>()
            for (c in 0 until capsJson.length()) {
                val versJson = capsJson.getJSONArray(c)
                val vers = mutableListOf<String>()
                for (v in 0 until versJson.length()) vers.add(versJson.getString(v))
                caps.add(vers)
            }
            lista.add(Libro(nombre, caps))
        }
        return lista
    }

    private fun cargarRV1960(context: Context): List<Libro> {
        val raw = context.assets.open("biblia_rv1960.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(raw.removePrefix("\uFEFF"))
        val mapaClaves = mutableMapOf<String, String>()
        val keys = root.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            mapaClaves[normalizar(k)] = k
        }
        val lista = mutableListOf<Libro>()
        for (nombreOficial in nombresLibros) {
            val norm = normalizar(nombreOficial)
            val claveReal = mapaClaves[norm]
                ?: mapaClaves.entries.firstOrNull { it.key.contains(norm) || norm.contains(it.key) }?.value
            if (claveReal != null) {
                val capsObj = root.getJSONObject(claveReal)
                val capNums = mutableListOf<Int>()
                val cKeys = capsObj.keys()
                while (cKeys.hasNext()) cKeys.next().toIntOrNull()?.let { capNums.add(it) }
                capNums.sort()
                val caps = mutableListOf<List<String>>()
                for (cNum in capNums) {
                    val versObj = capsObj.getJSONObject(cNum.toString())
                    val verNums = mutableListOf<Int>()
                    val vKeys = versObj.keys()
                    while (vKeys.hasNext()) vKeys.next().toIntOrNull()?.let { verNums.add(it) }
                    verNums.sort()
                    val vers = verNums.map { versObj.getString(it.toString()).trim() }
                    caps.add(vers)
                }
                lista.add(Libro(nombreOficial, caps))
            }
        }
        return if (lista.isNotEmpty()) lista else cargarRV1909(context)
    }

    fun buscarConcordancia(context: Context, version: String, consulta: String, limite: Int = 300): List<ResultadoConcordancia> {
        val q = normalizar(consulta)
        if (q.length < 2) return emptyList()
        val libros = obtenerLibros(context, version)
        val resultados = mutableListOf<ResultadoConcordancia>()
        for (l in libros.indices) {
            val libro = libros[l]
            for (c in libro.capitulos.indices) {
                val cap = libro.capitulos[c]
                for (v in cap.indices) {
                    val texto = cap[v]
                    if (normalizar(texto).contains(q)) {
                        resultados.add(ResultadoConcordancia(l, c, v, "${libro.nombre} ${c + 1}:${v + 1}", texto))
                        if (resultados.size >= limite) return resultados
                    }
                }
            }
        }
        return resultados
    }
}
