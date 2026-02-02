package com.github.heroslender.herochat.utils

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.server.core.ui.builder.EventData
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder

fun UIEventBuilder.bind(
    type: CustomUIEventBindingType,
    selector: String,
    vararg data: Pair<String, String>,
    locksInterface: Boolean = false
) = addEventBinding(type, selector, data.let { datas ->
    EventData().apply {
        for (d in datas) {
            put(d.first, d.second)
        }
    }
}, locksInterface)

fun UIEventBuilder.onValueChanged(
    selector: String,
    vararg data: Pair<String, String>,
    locksInterface: Boolean = false
) = onEvent(CustomUIEventBindingType.ValueChanged, selector, *data, locksInterface = locksInterface)

fun UIEventBuilder.onActivating(
    selector: String,
    vararg data: Pair<String, String>,
    locksInterface: Boolean = false
) = onEvent(CustomUIEventBindingType.Activating, selector, *data, locksInterface = locksInterface)

fun UIEventBuilder.onEvent(
    eventType: CustomUIEventBindingType,
    selector: String,
    vararg data: Pair<String, String>,
    locksInterface: Boolean = false
) = bind(eventType, selector, *data, locksInterface = locksInterface)