package com.kenny.atd.account.vo

import com.kenny.atd.shared.BankCode

data class AccountNumber(
    val bankCode: BankCode,
    val accountNumber: String,
)