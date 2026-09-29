package com.rafael.googlenocarro

import android.content.Context

/** Guarda as últimas pesquisas feitas (compartilhado entre carro e celular). */
object History {
    private const val PREFS = "historico"
    private const val KEY = "itens"
    private const val MAX = 10

    fun get(context: Context): List<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "")!!
            .split('\n')
            .filter { it.isNotBlank() }

    fun add(context: Context, query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        val list = (listOf(q) + get(context).filter { !it.equals(q, ignoreCase = true) }).take(MAX)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, list.joinToString("\n")).apply()
    }
}
