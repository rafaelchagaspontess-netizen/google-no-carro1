package com.rafael.googlenocarro

import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

/** Acesso à internet: sugestões do Google e um resumo em texto para exibir/ler no carro. */
object GoogleApi {

    private const val UA = "GoogleNoCarro/1.0 (Android; app pessoal)"

    private fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("User-Agent", UA)
        try {
            val charset = conn.contentType
                ?.substringAfter("charset=", "")
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { Charset.forName(it.trim()) }.getOrNull() }
                ?: Charsets.UTF_8
            return conn.inputStream.use { it.readBytes().toString(charset) }
        } finally {
            conn.disconnect()
        }
    }

    /** Sugestões de pesquisa do Google (autocomplete). */
    fun suggestions(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        val url = "https://suggestqueries.google.com/complete/search?client=firefox" +
            "&hl=pt-BR&ie=UTF-8&oe=UTF-8&q=" + Uri.encode(query)
        val arr = JSONArray(get(url)).getJSONArray(1)
        return (0 until arr.length()).map { arr.getString(it) }
    }

    data class Answer(val title: String, val text: String)

    /** Resumo em texto sobre o assunto pesquisado (fonte: Wikipédia em português). */
    fun answer(query: String): Answer? {
        val url = "https://pt.wikipedia.org/w/api.php?action=query&format=json" +
            "&generator=search&gsrlimit=1&prop=extracts&exintro=1&explaintext=1" +
            "&exsentences=6&redirects=1&gsrsearch=" + Uri.encode(query)
        val pages = JSONObject(get(url)).optJSONObject("query")?.optJSONObject("pages") ?: return null
        val key = pages.keys().asSequence().firstOrNull() ?: return null
        val page = pages.getJSONObject(key)
        val text = page.optString("extract").trim()
        if (text.isEmpty()) return null
        return Answer(page.optString("title", query), text)
    }

    fun googleSearchUrl(query: String): String =
        "https://www.google.com/search?hl=pt-BR&q=" + Uri.encode(query)
}
