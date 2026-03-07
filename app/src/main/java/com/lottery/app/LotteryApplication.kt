package com.lottery.app

import android.app.Application
import com.lottery.app.di.AppContainer

class LotteryApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
