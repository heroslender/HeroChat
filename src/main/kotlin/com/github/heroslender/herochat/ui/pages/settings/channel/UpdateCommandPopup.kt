package com.github.heroslender.herochat.ui.pages.settings.channel

import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.popup.StringSupplierPopup

class UpdateCommandPopup<T: StringSupplierPopup.EventData>(
    parent: Page<T>,
    value: String? = null,
    onConfirm: StringSupplierPopup<T>.(value: String?, data: T) -> Unit,
) : StringSupplierPopup<T>(
    parent,
    if (value == null) "New Command" else "Edit command",
    "Command",
    value,
    "Save",
    "Cancel",
    { null },
    { closePopup() },
    onConfirm
) {
    interface EventData: StringSupplierPopup.EventData
}