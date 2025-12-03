package com.kenny.atd.account

import com.kenny.atd.shared.Money

class Account(
    private val accountNo: String,
    private val balance: Money
) {
    fun withdraw( amount: Money ): Account {
        return this
    }

    fun deposit( amount: Money ): Account {
        return this
    }
}