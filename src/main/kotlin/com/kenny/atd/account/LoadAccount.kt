package com.kenny.atd.account

import org.springframework.stereotype.Component

@Component
class LoadAccount(
    private val accountMainEntityRepository: AccountMainEntityRepository
) {
    fun execute(accountNumber: String): Account {
        // TODO: Account 도메인 엔터티 생성 로직
//        return Account( accountNumber, Money(BigDecimal.ZERO))
    }


}