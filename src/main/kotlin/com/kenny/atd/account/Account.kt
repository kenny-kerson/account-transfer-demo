package com.kenny.atd.account

import com.kenny.atd.shared.BankCode
import com.kenny.atd.shared.Money

class Account(
    val accountNumber: AccountNumber,
    val balance: Money
) {
    fun withdraw( amount: Money ): Account {
        return this
    }

    fun deposit( amount: Money ): Account {
        return this
    }
}