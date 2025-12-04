package com.kenny.atd.account

enum class AccountStatus(val code: String) {
    NORMAL("01"),
    STOPPED("02"),
    CLOSED("09")
    ;

    companion object {
        fun fromCode(code: String): AccountStatus {
            // TODO: 이 코드의 로직에 대해 세부적인 이해 필요
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown account status: $code")
        }
    }
}