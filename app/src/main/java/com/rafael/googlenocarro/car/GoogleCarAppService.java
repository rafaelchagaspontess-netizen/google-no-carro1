package com.rafael.googlenocarro.car;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

/** Ponto de entrada do Android Auto. */
public class GoogleCarAppService extends CarAppService {

    @NonNull
    @Override
    public HostValidator createHostValidator() {
        // App de uso pessoal (instalado fora da Play Store): aceita qualquer host do Android Auto.
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        return new Session() {
            @NonNull
            @Override
            public Screen onCreateScreen(@NonNull Intent intent) {
                return new SearchScreen(getCarContext());
            }
        };
    }
}
