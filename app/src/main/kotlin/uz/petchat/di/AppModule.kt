package uz.petchat.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import uz.petchat.BuildConfig
import uz.petchat.core.mvi.MviLogger

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMviLogger(): MviLogger =
        if (BuildConfig.DEBUG) LogcatMviLogger else MviLogger.None

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}

private object LogcatMviLogger : MviLogger {
    override val isEnabled: Boolean = true

    override fun log(tag: String, message: String) {
        Log.d("MVI/$tag", message)
    }
}
