package com.kenny.atd.transfer

import com.kenny.atd.account.LoadAccount
import com.kenny.atd.account.vo.AccountId
import com.kenny.atd.transfer.entity.Transfer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TransferDomainService(
    val loadAccount: LoadAccount,
) {
    private val logger = LoggerFactory.getLogger(TransferDomainService::class.java)

    fun transfer(
        transfer: Transfer,
        fromAccountId: AccountId,
        toAccountId: AccountId,
        transferAmount: String,
    ) {
        logger.debug("[kenny] Transfer Domain Service received")
        loadAccount.execute(fromAccountId.accountNumber)
            .withdraw(transfer.amount)
        loadAccount.execute(toAccountId.accountNumber)
            .deposit(transfer.amount)
        // TODO: 이체 도메인 로직 처리 추가 필요!
    }
}