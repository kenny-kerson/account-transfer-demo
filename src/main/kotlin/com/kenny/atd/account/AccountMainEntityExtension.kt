package com.kenny.atd.account

import com.kenny.atd.shared.BankCode
import com.kenny.atd.shared.Money

fun AccountMainEntity.toDomain(): Account {
    return Account(
        accountNumber = AccountNumber(
            bankCode = BankCode.valueOf(bankCode),
            accountNumber = accountNumber
        ),
        balance = Money(balance),
        accountStatus = AccountStatus.valueOf(accountStatus)
    )
}

fun AccountMainEntity.toEntity(account: Account): AccountMainEntity {
    return AccountMainEntity(
        bankCode = account.accountNumber.bankCode.code,
        accountNumber = account.accountNumber.accountNumber,
        accountStatus = account.accountStatus.code,
        balance = account.balance.amount
    )
}