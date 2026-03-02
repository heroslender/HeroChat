package com.github.heroslender.herochat.utils

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import java.util.*
import kotlin.reflect.KMutableProperty1

fun <T, V> BuilderCodec.Builder<T>.append(
    prop: KMutableProperty1<T, V>,
    codec: Codec<V>,
    name: String = prop.getConfigName(),
    required: Boolean = false,
    default: (() -> V)? = null
): BuilderCodec.Builder<T> = append(
    KeyedCodec(name, codec, required),
    { config, value -> prop.set(config, if (default != null) value ?: default() else value) },
    { config -> prop.get(config) }
).add()

fun <T> BuilderCodec.Builder<T>.appendString(
    prop: KMutableProperty1<T, String>,
    name: String = prop.getConfigName(),
): BuilderCodec.Builder<T> = append(prop, Codec.STRING, name = name, required = true)

fun <T> BuilderCodec.Builder<T>.appendStringOpt(
    prop: KMutableProperty1<T, String?>,
    name: String = prop.getConfigName(),
): BuilderCodec.Builder<T> = append(prop, Codec.STRING, name = name, required = false)

fun <T> BuilderCodec.Builder<T>.appendBoolean(
    prop: KMutableProperty1<T, Boolean>,
    name: String = prop.getConfigName(),
): BuilderCodec.Builder<T> = append(prop, Codec.BOOLEAN, name = name, required = true)

fun <T> BuilderCodec.Builder<T>.appendBooleanOpt(
    prop: KMutableProperty1<T, Boolean?>,
    name: String = prop.getConfigName(),
): BuilderCodec.Builder<T> = append(prop, Codec.BOOLEAN, name = name, required = false)

fun KMutableProperty1<*, *>.getConfigName(): String {
    return name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}