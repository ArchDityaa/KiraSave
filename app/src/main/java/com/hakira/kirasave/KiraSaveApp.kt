package com.hakira.kirasave

import android.app.Application
import android.content.Context
import com.hakira.kirasave.data.AppDatabase
import com.hakira.kirasave.data.DownloadEngine
import com.hakira.kirasave.data.SettingsRepository
import com.hakira.kirasave.data.TikTokRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class KiraSaveApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = AppDatabase.create(appContext)
    val settings = SettingsRepository(appContext)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    val repository = TikTokRepository(client, settings)
    val downloads = DownloadEngine(appContext, database.downloadDao(), settings, client)
}
