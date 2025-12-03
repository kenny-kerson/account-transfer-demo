package com.kenny.atd.transfer

import com.kenny.atd.account.Account
import com.kenny.atd.shared.Money

class Transfer {
    fun transfer(
        fromAccount: Account,
        toAccount: Account,

        money: Money
    ) {
        fromAccount.withdraw( money )
        toAccount.deposit( money )
    }
}