package uz.petchat.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import uz.petchat.data.repository.DefaultConnectionRepository
import uz.petchat.data.repository.MockChatRepository
import uz.petchat.domain.repository.ChatRepository
import uz.petchat.domain.repository.ConnectionRepository
import uz.petchat.domain.repository.MessageRepository

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
internal annotation class DataScope

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindChatRepository(impl: MockChatRepository): ChatRepository

    @Binds
    abstract fun bindMessageRepository(impl: MockChatRepository): MessageRepository

    @Binds
    abstract fun bindConnectionRepository(impl: DefaultConnectionRepository): ConnectionRepository

    companion object {
        @Provides
        @Singleton
        @DataScope
        fun provideDataScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
