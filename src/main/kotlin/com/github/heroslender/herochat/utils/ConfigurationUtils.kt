package com.github.heroslender.herochat.utils

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import java.util.Locale
import kotlin.reflect.KMutableProperty1

fun <T, V> BuilderCodec.Builder<T>.append(
    prop: KMutableProperty1<T, V>,
    codec: Codec<V>,
    required: Boolean = false,
    default: (() -> V)? = null
): BuilderCodec.Builder<T> = append(
    KeyedCodec(prop.getConfigName(), codec, required),
    { config, value -> prop.set(config, if (default != null) value ?: default() else value) },
    { config -> prop.get(config) }
).add()

fun <T> BuilderCodec.Builder<T>.appendString(
    prop: KMutableProperty1<T, String>,
): BuilderCodec.Builder<T> = append(prop, Codec.STRING, true)

fun <T> BuilderCodec.Builder<T>.appendBoolean(
    prop: KMutableProperty1<T, Boolean>,
): BuilderCodec.Builder<T> = append(prop, Codec.BOOLEAN, true)

fun <T> BuilderCodec.Builder<T>.appendStringOpt(
    prop: KMutableProperty1<T, String?>,
): BuilderCodec.Builder<T> = append(prop, Codec.STRING, false)

fun KMutableProperty1<*, *>.getConfigName(): String {
    return name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}