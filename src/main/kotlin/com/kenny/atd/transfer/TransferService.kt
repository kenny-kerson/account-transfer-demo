package com.kenny.atd.transfer

import com.kenny.atd.account.LoadAccount
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TransferService(
    val loadAccount: LoadAccount,
) {
    private val logger = LoggerFactory.getLogger(TransferService::class.java)

    // 입력은 표현계층 값을 그대로 받고, 출력은 도메인 엔터티를 그대로 리턴한다
    fun transfer(
        fromAccountNumber: String,
        toAccountNumber: String,
        transferAmount: String,
    ) {
        logger.debug("[kenny] Transfer Service received")
    }
}