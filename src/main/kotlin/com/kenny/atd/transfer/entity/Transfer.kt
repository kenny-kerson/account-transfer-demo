package com.kenny.atd.transfer.entity

import com.kenny.atd.account.entity.Account
import com.kenny.atd.account.entity.AccountId
import com.kenny.atd.account.vo.AccountNumber
import com.kenny.atd.shared.Money
import com.kenny.atd.transfer.entity.TransferId
import org.slf4j.LoggerFactory

class Transfer(
    val id: TransferId,
    val fromAccount: AccountId,
    val toAccount: AccountId,
    val amount: Money,

) {
    // TODO: 이 클래스가 도메인 엔터티인지, 도메인 서비스인지 그 정체성에 대한 확인이 필요함. 프로퍼티가 없고, 타 엔터티들을 엮어주는 역할을 하므로 도메인 서비스라는 의견이 있음
    private val logger = LoggerFactory.getLogger(Transfer::class.java)

    fun execute() {
        logger.debug("[kenny] Transfer received")
        fromAccount.withdraw( amount )
        toAccount.deposit( amount )
    }
}