package com.kenny.atd.account

import com.kenny.atd.shared.Money
import org.springframework.stereotype.Repository

@Repository
class AccountMainEntityRepository {
    fun findById(accountNumber: String): AccountMainEntity {
        // TODO: 실제 데이터 접근기술을 통해 테이블 엔터티를 조회하는 로직 추가 필요. 엔터티가 존재하지 않을때는 익셉션 쓰로우
        return AccountMainEntity(
            accountNumber = "33331231234",
            balance = "10000".toBigDecimal(),
            accountStatus = AccountStatus.NORMAL
        )
    }
}