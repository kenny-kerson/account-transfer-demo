package com.kenny.atd.account

import com.kenny.atd.shared.Money
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class LoadAccount {
    fun execute( accountNumber: String ): Account {
        // TODO: Account 도메인 엔터티 생성 로직
        return Account( accountNumber, Money(BigDecimal.ZERO))
    }
}