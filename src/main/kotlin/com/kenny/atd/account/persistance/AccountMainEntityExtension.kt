package com.kenny.atd.account.persistance

import com.kenny.atd.account.AccountStatus
import com.kenny.atd.account.entity.Account
import com.kenny.atd.account.entity.AccountId
import com.kenny.atd.shared.BankCode
import com.kenny.atd.shared.Money

fun AccountMainEntity.toDomain(): Account {
    return Account(
        id = AccountId(
            accountNumber = accountNumber,
            bankCode = BankCode.valueOf(bankCode)
        ),
        balance = Money(balance),
        accountStatus = AccountStatus.valueOf(accountStatus)
    )
}

fun AccountMainEntity.toEntity(account: Account): AccountMainEntity {
    return AccountMainEntity(
        bankCode = account.id.accountNumber,
        accountNumber = account.id.accountNumber,
        accountStatus = account.accountStatus.code,
        balance = account.balance.amount
    )
}