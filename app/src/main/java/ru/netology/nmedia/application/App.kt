package ru.netology.nmedia.application

import android.app.Application
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.di.DependencyContainer

class App: Application() {
    override fun onCreate() {
        super.onCreate()
//        AppAuth.Companion.init(this)
        DependencyContainer.initApp(this)
    }
}