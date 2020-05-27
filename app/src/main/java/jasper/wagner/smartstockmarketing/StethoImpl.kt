package jasper.wagner.smartstockmarketing

import android.app.Application
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient


class StethoImpl : Application() {
    override fun onCreate() {
        super.onCreate()
        initStetho()
    }

    fun initStetho() {
        Stetho.initialize(
            Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build())
        val client = OkHttpClient().newBuilder()
        client.addNetworkInterceptor(StethoInterceptor())
    }
}