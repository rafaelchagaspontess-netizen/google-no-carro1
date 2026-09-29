package com.rafael.googlenocarro.car

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.rafael.googlenocarro.GoogleApi
import com.rafael.googlenocarro.History
import java.util.Locale

/** Mostra (e lê em voz alta) um resumo do que foi pesquisado. */
class ResultScreen(ctx: CarContext, private val query: String) : Screen(ctx) {

    private var title = query
    private var message = "Pesquisando “$query”…"
    private var loaded = false
    private var speaking = false
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        History.add(ctx, query)
        tts = TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setLanguage(Locale("pt", "BR"))
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        main.post { speaking = false; invalidate() }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        main.post { speaking = false; invalidate() }
                    }
                })
                ttsReady = true
                if (loaded) speak()
            }
        }
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                tts?.stop(); tts?.shutdown(); tts = null
            }
        })
        background({ GoogleApi.answer(query) }) { r ->
            val a = r.getOrNull()
            when {
                r.isFailure -> message = "Sem conexão com a internet. Tente novamente."
                a == null -> message = "Não encontrei um resumo para “$query”. " +
                    "Se for um lugar, toque em “Ir até lá”. A pesquisa completa fica salva no app do celular."
                else -> { title = a.title; message = a.text }
            }
            loaded = true
            invalidate()
            if (ttsReady && r.isSuccess && a != null) speak()
        }
    }

    private fun speak() {
        val t = tts ?: return
        speaking = true
        t.speak("$title. $message", TextToSpeech.QUEUE_FLUSH, null, "resposta")
        invalidate()
    }

    private fun stop() {
        tts?.stop()
        speaking = false
        invalidate()
    }

    override fun onGetTemplate(): Template {
        val listen = Action.Builder()
            .setTitle(if (speaking) "Parar" else "Ouvir")
            .setOnClickListener { if (speaking) stop() else speak() }
            .build()
        val navigate = Action.Builder()
            .setTitle("Ir até lá")
            .setOnClickListener {
                try {
                    carContext.startCarApp(
                        Intent(CarContext.ACTION_NAVIGATE, Uri.parse("geo:0,0?q=" + Uri.encode(query)))
                    )
                } catch (e: Exception) {
                    CarToast.makeText(carContext, "Não foi possível abrir a navegação", CarToast.LENGTH_LONG).show()
                }
            }
            .build()

        val b = MessageTemplate.Builder(message.take(1200))
            .setTitle(title)
            .setHeaderAction(Action.BACK)
        if (loaded) b.addAction(listen).addAction(navigate)
        return b.build()
    }
}
