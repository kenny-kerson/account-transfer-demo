package com.kenny.atd.transfer

import com.kenny.atd.account.LoadAccount
import com.kenny.atd.shared.Money
import com.kenny.atd.transfer.entity.Transfer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TransferService(
    private val loadAccount: LoadAccount,
) {
    private val logger = LoggerFactory.getLogger(TransferService::class.java)
    // TODO: 이런 식으로 구현함에 OOD 혹은 기술적인 문제가 없을지?
    private val transfer = Transfer()

    fun transfer(
        fromAccountNumber: String,
        toAccountNumber: String,
        transferAmount: String,
    ) {
        logger.debug("[kenny] Transfer received")
        val fromAccount = loadAccount.execute(fromAccountNumber)
        val toAccount = loadAccount.execute(toAccountNumber)
        transfer.execute(
            fromAccount,
            toAccount,
            Money(transferAmount.toBigDecimal())
        )
    }
}