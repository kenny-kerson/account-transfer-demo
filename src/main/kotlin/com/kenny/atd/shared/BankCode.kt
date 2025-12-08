package com.kenny.atd.shared

import com.kenny.atd.account.AccountStatus

enum class BankCode(
    val code: String,
) {
    KAKAOBANK("091"),
    KBANK("090"),
    NONGHYUP("011")
    ;

    companion object {
        fun fromCode(code: String): BankCode {
            return BankCode.entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown account status: $code")
        }
    }
}