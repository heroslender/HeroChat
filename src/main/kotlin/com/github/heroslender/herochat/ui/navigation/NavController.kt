package com.github.heroslender.herochat.ui.navigation

import com.github.heroslender.herochat.ui.SubPage
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class NavController<D>(
    startDestination: String,
    val contentComponentSelector: String,
) {
    var currentLocation: String = startDestination
        private set
    private val pages = mutableMapOf<String, Destination<D>>()

    val currentDestination: Destination<D>?
        get() = pages[currentLocation]
    val currentPage: SubPage<D>?
        get() = currentDestination?.subPage

    fun addNavLocation(
        location: String,
        onEnter: (cmd: UICommandBuilder) -> Unit = {},
        onLeave: (cmd: UICommandBuilder) -> Unit = {},
        pageInitializer: () -> SubPage<D>,
    ) {
        pages[location] = Destination(onEnter, onLeave, pageInitializer)
    }

    fun navigateTo(
        destination: String,
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        if (currentLocation == destination) {
            return
        }

        setPage(destination, ref, cmd, evt, store)
    }

    fun build(
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        setPage(currentLocation, ref, cmd, evt, store)
    }

    private fun setPage(
        location: String,
        ref: Ref<EntityStore?>,
        cmd: UICommandBuilder,
        evt: UIEventBuilder,
        store: Store<EntityStore?>
    ) {
        val previousPage = currentDestination
        val destination = pages[location]
            ?: throw IllegalArgumentException("Destination does not exist: $location")
        val page = destination.getSubPageOrInit()

        cmd.clear(contentComponentSelector)
        cmd.append(contentComponentSelector, page.layoutPath)
        page.build(ref, cmd, evt, store)

        currentLocation = location

        previousPage?.onLeave(cmd)
        destination.onEnter(cmd)
    }
}