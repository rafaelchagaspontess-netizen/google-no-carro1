package com.rafael.googlenocarro.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import com.rafael.googlenocarro.GoogleApi
import com.rafael.googlenocarro.R

/** Busca com teclado do carro e sugestões do Google em tempo real. */
class SearchScreen(ctx: CarContext) : Screen(ctx) {

    private var text = ""
    private var suggestions: List<String> = emptyList()
    private var requestId = 0
    private val debounce = Runnable { fetch() }

    private fun fetch() {
        val q = text
        val id = ++requestId
        if (q.isBlank()) { suggestions = emptyList(); invalidate(); return }
        background({ GoogleApi.suggestions(q) }) { r ->
            if (id != requestId) return@background
            suggestions = r.getOrDefault(emptyList())
            invalidate()
        }
    }

    private fun open(q: String) {
        if (q.isBlank()) return
        screenManager.push(ResultScreen(carContext, q.trim()))
    }

    override fun onGetTemplate(): Template {
        val items = ItemList.Builder()
        val shown = if (text.isNotBlank() && suggestions.none { it.equals(text.trim(), true) })
            listOf(text.trim()) + suggestions else suggestions
        shown.take(6).forEach { s ->
            items.addItem(
                Row.Builder()
                    .setTitle(s)
                    .setImage(icon(carContext, R.drawable.ic_search))
                    .setOnClickListener { open(s) }
                    .build()
            )
        }
        if (shown.isEmpty()) items.setNoItemsMessage("Digite algo para ver sugestões do Google")

        return SearchTemplate.Builder(object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {
                text = searchText
                main.removeCallbacks(debounce)
                main.postDelayed(debounce, 300)
            }

            override fun onSearchSubmitted(searchText: String) = open(searchText)
        })
            .setHeaderAction(Action.BACK)
            .setSearchHint("Pesquisar no Google")
            .setInitialSearchText(text)
            .setShowKeyboardByDefault(true)
            .setItemList(items.build())
            .build()
    }
}
