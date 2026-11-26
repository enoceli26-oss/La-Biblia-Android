package com.biblia.rv1909

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer

data class Libro(val nombre: String, val capitulos: List<List<String>>)
data class ResultadoConcordancia(val libroIdx: Int, val capIdx: Int, val verIdx: Int, val cita: String, val texto: String)

object BibleData {
    private val cache = mutableMapOf<String, List<Libro>>()

    val nombresLibrosEs = listOf(
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

    val nombresLibrosEn = listOf(
        "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
        "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
        "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
        "Nehemiah", "Esther", "Job", "Psalms", "Proverbs",
        "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations",
        "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
        "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk",
        "Zephaniah", "Haggai", "Zechariah", "Malachi",
        "Matthew", "Mark", "Luke", "John", "Acts",
        "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians",
        "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians",
        "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews",
        "James", "1 Peter", "2 Peter", "1 John", "2 John",
        "3 John", "Jude", "Revelation"
    )

    val nombresLibros get() = nombresLibrosEs

    private fun normalizar(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase().trim()

    fun isEnglishVersion(version: String): Boolean = version in listOf("KJV", "WEB")

    fun obtenerLibros(context: Context, version: String = "RV1909"): List<Libro> {
        val effLang = try {
            context.getSharedPreferences("biblia_prefs", Context.MODE_PRIVATE).getString("app_lang", "es") ?: "es"
        } catch (_: Exception) { "es" }
        val key = "${version}_$effLang"
        cache[key]?.let { return it }
        val nombres = if (effLang == "en") nombresLibrosEn else nombresLibrosEs
        val rawList = try {
            when (version) {
                "RV1960" -> cargarPorClave(context, "biblia_rv1960.json", nombresLibrosEs)
                "KJV" -> cargarArray(context, "biblia_kjv.json")
                "WEB" -> cargarArray(context, "biblia_web.json")
                else -> cargarArray(context, "biblia_rv1909.json")
            }
        } catch (_: Exception) {
            cargarArray(context, "biblia_rv1909.json")
        }
        val safeRaw = if (rawList.isNotEmpty()) rawList else cargarArray(context, "biblia_rv1909.json")
        val finalList = safeRaw.mapIndexed { idx, lib ->
            Libro(if (idx < nombres.size) nombres[idx] else lib.nombre, lib.capitulos)
        }
        cache[key] = finalList
        return finalList
    }

    private fun cargarArray(context: Context, assetName: String): List<Libro> {
        val raw = context.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val jsonArray = JSONArray(raw.removePrefix("\uFEFF").trim())
        val lista = mutableListOf<Libro>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val nombre = if (i < nombresLibrosEs.size) nombresLibrosEs[i] else "Book ${i+1}"
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

    private fun parseBooksArray(arr: JSONArray): List<Libro> {
        val lista = mutableListOf<Libro>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val capsArr = obj.getJSONArray("chapters")
            val caps = mutableListOf<List<String>>()
            for (c in 0 until capsArr.length()) {
                val cObj = capsArr.getJSONObject(c)
                val vArr = cObj.getJSONArray("verses")
                val vers = mutableListOf<String>()
                for (v in 0 until vArr.length()) vers.add(vArr.getJSONObject(v).getString("text").trim())
                caps.add(vers)
            }
            lista.add(Libro(obj.optString("name", "Book ${i+1}"), caps))
        }
        return lista
    }

    private fun cargarPorClave(context: Context, assetName: String, refNames: List<String>): List<Libro> {
        val raw = context.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val text = raw.removePrefix("\uFEFF").trim()
        if (text.startsWith("[")) return parseBooksArray(JSONArray(text))
        val root = JSONObject(text)
        if (root.has("books")) return parseBooksArray(root.getJSONArray("books"))
        val mapaClaves = mutableMapOf<String, String>()
        val keys = root.keys()
        while (keys.hasNext()) { val k = keys.next(); mapaClaves[normalizar(k)] = k }
        val lista = mutableListOf<Libro>()
        for (nombreOficial in refNames) {
            val norm = normalizar(nombreOficial)
            val claveReal = mapaClaves[norm] ?: mapaClaves.entries.firstOrNull { it.key.contains(norm) || norm.contains(it.key) }?.value
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
                    caps.add(verNums.map { versObj.getString(it.toString()).trim() })
                }
                lista.add(Libro(nombreOficial, caps))
            }
        }
        return if (lista.isNotEmpty()) lista else cargarArray(context, "biblia_rv1909.json")
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
