package com.cglhustle.core.common.error

enum class RecoveryAction {
    RETRY_SILENTLY,
    PROMPT_LOGIN,
    SHOW_TOAST,
    FATAL_HALT
}
