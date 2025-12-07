package com.kenny.atd.account

import com.kenny.atd.shared.Money

fun AccountMainEntity.toDomain(): Account {
    return Account(
        accountNumber = accountNumber,
        balance = Money(balance)
    )
}

fun AccountMainEntity.toEntity(account: Account): AccountMainEntity {
    return AccountMainEntity(
        accountNumber = account.accountNumber,
        balance = account.balance.amount,
        accountStatus = accountStatus
    )
}