package com.github.heroslender.herochat.ui.pages.settings.automod

import com.github.heroslender.herochat.ui.Page
import com.github.heroslender.herochat.ui.popup.StringSupplierPopup

class RulePatternPopup<T : RulePatternPopup.EventData>(
    parent: Page<T>,
    value: String? = null,
    onConfirm: StringSupplierPopup<T>.(value: String?, data: T) -> Unit,
) : StringSupplierPopup<T>(
    parent = parent,
    title = if (value == null) "New Pattern" else "Edit Pattern",
    label = "Pattern",
    value = value,
    confirmBtnText = "Save",
    cancelBtnText = "Cancel",
    valueValidator = { null },
    onCancel = { closePopup() },
    onConfirm = onConfirm,
) {

    interface EventData : StringSupplierPopup.EventData
}