package com.github.heroslender.herochat.ui.pages.settings

import com.github.heroslender.herochat.ui.pages.settings.ComponentManagerEventData.Companion.appendComponentManagerEventData
import com.github.heroslender.herochat.ui.pages.settings.automod.AutomodEventData
import com.github.heroslender.herochat.ui.pages.settings.automod.AutomodEventData.Companion.appendAutomodEventData
import com.github.heroslender.herochat.ui.pages.settings.channel.ChannelEventData
import com.github.heroslender.herochat.ui.pages.settings.channel.ChannelEventData.Companion.appendChannelEventData
import com.github.heroslender.herochat.ui.pages.settings.general.SettingsEventData
import com.github.heroslender.herochat.ui.pages.settings.general.SettingsEventData.Companion.appendSettingsEventData
import com.github.heroslender.herochat.ui.pages.settings.privatechannel.PrivateChannelEventData
import com.github.heroslender.herochat.ui.pages.settings.privatechannel.PrivateChannelEventData.Companion.appendPrivateChannelEventData
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

class UiState: SettingsEventData, AutomodEventData, ChannelEventData, PrivateChannelEventData {
    override var action: String? = null

    // Navigation
    var navigateTo: String? = null

    // Component popup data
    override var componentId: String? = null
    override var componentText: String? = null
    override var componentPermission: String? = null

    // Settings page props
    override var defaultChannel: String? = null
    override var nicknameLength: Int? = null

    // Automod page props
    override var automodEnabled: Boolean? = null
    override var automodDefaultBlockMessage: String? = null

    override var ruleIndex: Int? = null
    override var rulePattern: Array<String>? = null
    override var ruleIsRegex: Boolean? = null
    override var ruleReplacement: String? = null
    override var ruleBlockMessage: String? = null
    override var rulePopupPattern: String? = null

    // Private channel props
    override var receiverFormat: String? = null

    // Channel page props
    override var format: String? = null
    override var permission: String? = null
    override var crossWorld: Boolean? = null
    override var distance: Double? = null

    override var capslockFilterEnabled: Boolean? = null
    override var capslockFilterPercentage: Int? = null
    override var capslockFilterMinLength: Int? = null

    companion object {
        val CODEC: BuilderCodec<UiState> = BuilderCodec.builder(
            UiState::class.java, { UiState() })
            .append(
                KeyedCodec("Action", Codec.STRING),
                { e, v -> e.action = v },
                { e -> e.action }).add()
            .append(
                KeyedCodec("NavigateTo", Codec.STRING),
                { e, v -> e.navigateTo = v },
                { e -> e.navigateTo }).add()
            .appendComponentManagerEventData()
            .appendSettingsEventData()
            .appendAutomodEventData()
            .appendPrivateChannelEventData()
            .appendChannelEventData()
            .build()
    }
}