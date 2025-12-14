package com.kenny.atd.transfer

import com.kenny.atd.account.LoadAccount
import com.kenny.atd.account.entity.Account
import com.kenny.atd.account.vo.AccountId
import com.kenny.atd.shared.Money
import com.kenny.atd.transfer.entity.Transfer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TransferService(
    val loadAccount: LoadAccount,
) {
    private val logger = LoggerFactory.getLogger(TransferService::class.java)

    // TODO: DDD 전술적 설계에서의 어플리케이션 서비스와 도메인 서비스가 통합된 형태인데 문제가 없을지 검토 필요
    fun transfer(
        transfer: Transfer,
        fromAccountId: AccountId,
        toAccountId: AccountId,
        transferAmount: String,
    ) {
        logger.debug("[kenny] Transfer received")
        loadAccount.execute(fromAccountId.accountNumber)
            .withdraw(transfer.amount)
        loadAccount.execute(toAccountId.accountNumber)
            .deposit(transfer.amount)
        // TODO: 이체 도메인 로직 처리 추가 필요!
    }
}