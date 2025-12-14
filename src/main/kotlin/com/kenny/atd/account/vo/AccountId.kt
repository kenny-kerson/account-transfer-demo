package com.kenny.atd.account.vo

import com.kenny.atd.shared.BankCode

data class AccountId(
    val accountNumber: String,
    val bankCode: BankCode,
)