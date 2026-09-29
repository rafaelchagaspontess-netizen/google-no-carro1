package com.rafael.googlenocarro.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.rafael.googlenocarro.History
import com.rafael.googlenocarro.R

/** Tela inicial no carro: pesquisar + pesquisas recentes. */
class HomeScreen(ctx: CarContext) : Screen(ctx) {

    init {
        // Atualiza a lista de recentes sempre que voltar para esta tela.
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = invalidate()
        })
    }

    override fun onGetTemplate(): Template {
        val list = ItemList.Builder()
        list.addItem(
            Row.Builder()
                .setTitle("Pesquisar no Google")
                .addText("Digite quando o carro estiver parado")
                .setImage(icon(carContext, R.drawable.ic_search))
                .setBrowsable(true)
                .setOnClickListener { screenManager.push(SearchScreen(carContext)) }
                .build()
        )
        History.get(carContext).take(5).forEach { q ->
            list.addItem(
                Row.Builder()
                    .setTitle(q)
                    .setImage(icon(carContext, R.drawable.ic_history))
                    .setBrowsable(true)
                    .setOnClickListener { screenManager.push(ResultScreen(carContext, q)) }
                    .build()
            )
        }
        return ListTemplate.Builder()
            .setTitle("Google no Carro")
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(list.build())
            .build()
    }
}
