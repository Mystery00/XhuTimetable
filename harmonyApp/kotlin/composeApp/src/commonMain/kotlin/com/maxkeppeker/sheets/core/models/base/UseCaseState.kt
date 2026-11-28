package com.maxkeppeker.sheets.core.models.base

class UseCaseState(
    var visible: Boolean = false,
) {
    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }
}
