package com.rafael.googlenocarro.car

import android.os.Handler
import android.os.Looper
import androidx.car.app.CarContext
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import java.util.concurrent.Executors

internal val io = Executors.newFixedThreadPool(2)
internal val main = Handler(Looper.getMainLooper())

/** Executa [work] em segundo plano e entrega o resultado na thread principal. */
internal fun <T> background(work: () -> T, done: (Result<T>) -> Unit) {
    io.execute {
        val r = runCatching(work)
        main.post { done(r) }
    }
}

internal fun icon(ctx: CarContext, res: Int): CarIcon =
    CarIcon.Builder(IconCompat.createWithResource(ctx, res)).build()
