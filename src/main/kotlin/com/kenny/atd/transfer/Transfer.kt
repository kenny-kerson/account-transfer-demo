package com.kenny.atd.transfer

import com.kenny.atd.account.Account
import com.kenny.atd.shared.Money
import org.slf4j.LoggerFactory

class Transfer {
    // TODO: 이 클래스가 도메인 엔터티인지, 도메인 서비스인지 그 정체성에 대한 확인이 필요함. 프로퍼티가 없고, 타 엔터티들을 엮어주는 역할을 하므로 도메인 서비스라는 의견이 있음
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