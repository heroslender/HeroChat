package com.github.heroslender.herochat.ui.pages.settings

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

interface ComponentManagerEventData {
    var componentId: String?
    var componentText: String?
    var componentPermission: String?

    object ActionType {
        const val NewComponent = "newComponent"
        const val EditComponent = "editComponent"
        const val DeleteComponent = "deleteComponent"
    }

    companion object {
        const val FieldComponentId = "@ComponentId"
        const val FieldComponentText = "@ComponentText"
        const val FieldComponentPermission = "@ComponentPermission"

        const val ComponentId = "ComponentId"

        fun <T : ComponentManagerEventData> BuilderCodec.Builder<T>.appendComponentManagerEventData(): BuilderCodec.Builder<T> {
            return this
                .append(
                    KeyedCodec(FieldComponentId, Codec.STRING),
                    { e, v -> e.componentId = v },
                    { e -> e.componentId }).add()
                .append(
                    KeyedCodec(FieldComponentText, Codec.STRING),
                    { e, v -> e.componentText = v },
                    { e -> e.componentText }).add()
                .append(
                    KeyedCodec(FieldComponentPermission, Codec.STRING),
                    { e, v -> e.componentPermission = v },
                    { e -> e.componentPermission }).add()
                .append(
                    KeyedCodec(ComponentId, Codec.STRING),
                    { e, v -> e.componentId = v },
                    { e -> e.componentId }).add()
        }
    }
}