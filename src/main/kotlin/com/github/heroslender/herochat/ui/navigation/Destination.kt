package com.github.heroslender.herochat.ui.navigation

import com.github.heroslender.herochat.ui.SubPage
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder

class Destination<D>(
    val onEnter: (cmd: UICommandBuilder) -> Unit = {},
    val onLeave: (cmd: UICommandBuilder) -> Unit = {},
    private val pageInitializer: () -> SubPage<D>,
) {
    var subPage: SubPage<D>? = null

    fun getSubPageOrInit(): SubPage<D> {
        var page = subPage
        if (page == null) {
            page = pageInitializer()
            this.subPage = page
        }

        return page
    }
}