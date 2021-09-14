package com.ivanovsky.passnotes.presentation.settings.database

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivanovsky.passnotes.data.repository.encdb.EncryptedDatabaseConfig
import com.ivanovsky.passnotes.data.repository.keepass.KeepassDatabaseConfig
import com.ivanovsky.passnotes.domain.interactor.ErrorInteractor
import com.ivanovsky.passnotes.domain.interactor.settings.database.DatabaseSettingsInteractor
import com.ivanovsky.passnotes.presentation.core.event.SingleLiveEvent
import kotlinx.coroutines.launch

class DatabaseSettingsViewModel(
    private val interactor: DatabaseSettingsInteractor,
    private val errorInteractor: ErrorInteractor
) : ViewModel() {

    val isLoading = MutableLiveData(true)
    val isRecycleBinEnabled = MutableLiveData(false)
    val errorMessage = SingleLiveEvent<String>()

    private var config: EncryptedDatabaseConfig? = null

    fun start() {
        isLoading.value = true

        viewModelScope.launch {
            val getConfig = interactor.getDbConfig()
            if (getConfig.isSucceededOrDeferred) {
                config = getConfig.obj

                isRecycleBinEnabled.value = config?.isRecycleBinEnabled ?: false
                isLoading.value = false
            } else {
                val message = errorInteractor.processAndGetMessage(getConfig.error)
                errorMessage.call(message)
            }
        }
    }

    fun onRecycleBinEnabledChanged(isEnabled: Boolean) {
        if (config == null) return

        isLoading.value = true

        viewModelScope.launch {
            val applyConfig = interactor.applyDbConfig(
                KeepassDatabaseConfig(
                    isRecycleBinEnabled = isEnabled
                )
            )

            if (applyConfig.isSucceededOrDeferred) {
                isRecycleBinEnabled.value = isEnabled
                isLoading.value = false
            } else {
                isRecycleBinEnabled.value = isRecycleBinEnabled.value
                val message = errorInteractor.processAndGetMessage(applyConfig.error)
                errorMessage.call(message)
            }
        }
    }
}