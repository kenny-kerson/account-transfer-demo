package com.kenny.atd.transfer

import com.kenny.atd.account.Account
import com.kenny.atd.shared.Money
import org.slf4j.LoggerFactory

class Transfer {
    private val logger = LoggerFactory.getLogger(Transfer::class.java)

    fun execute(
        fromAccount: Account,
        toAccount: Account,
        money: Money
    ) {
        logger.debug("[kenny] Transfer received")
        fromAccount.withdraw( money )
        toAccount.deposit( money )
    }
}