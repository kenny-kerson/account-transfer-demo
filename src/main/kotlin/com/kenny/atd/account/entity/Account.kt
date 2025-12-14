package com.kenny.atd.account.entity

import com.kenny.atd.account.vo.AccountNumber
import com.kenny.atd.account.AccountStatus
import com.kenny.atd.shared.Money

class Account(
    val id: AccountId,
    val accountStatus: AccountStatus,
    val balance: Money
) {
    fun withdraw( amount: Money): Account {
        return this
    }

    fun deposit( amount: Money): Account {
        return this
    }
}