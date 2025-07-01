
package com.yourcompany.geminiplugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.yourcompany.geminiplugin.settings.GeminiSettingsState",
    storages = [Storage("geminiSettings.xml")]
)
class GeminiSettingsState : PersistentStateComponent<GeminiSettingsState> {
    var apiKey: String = ""
    var promptHistory: MutableList<String> = mutableListOf()

    override fun getState(): GeminiSettingsState {
        return this
    }

    override fun loadState(state: GeminiSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): GeminiSettingsState {
            return ApplicationManager.getApplication().getService(GeminiSettingsState::class.java)
        }
    }
}
