package com.kenny.atd.account

import java.math.BigDecimal

data class AccountMainEntity(
    val accountNumber: String,
    val accountStatus: AccountStatus,
    val balance: BigDecimal,
)