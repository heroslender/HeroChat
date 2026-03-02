package com.github.heroslender.herochat.ui.event

interface ActionEventData {
    val action: String?

    companion object {
        const val Action = "Action"
    }
}