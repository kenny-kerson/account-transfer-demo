package com.kenny.atd.account

import com.kenny.atd.shared.BankCode
import java.math.BigDecimal

data class AccountMainEntity(
    // 테이블 엔터티와 도메인 엔터티가 명시적으로 분리되어 있기에,
    // 최대한 테이블의 컬럼 속성과 유사한 기본타입으로 변수 타입을 셋팅한다
    // 테이블 데이터를 Entity와 VO로 매핑하는 것은 Extension 함수에서 수행한다
    // 테이블의 데이터도 온전히 가지고, 도메인 Entity도 온전히 만듦으로써, 패러다임 불일치를 최소화한다
    val bankCode: String,
    val accountNumber: String,
    val accountStatus: String,
    val balance: BigDecimal,
)