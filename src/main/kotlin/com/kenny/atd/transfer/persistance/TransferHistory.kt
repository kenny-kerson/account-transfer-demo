package com.kenny.atd.transfer.persistance

import com.kenny.atd.account.entity.Account
import com.kenny.atd.transfer.TransferStatus
import com.kenny.atd.transfer.vo.TransferDateTime
import com.kenny.atd.user.UserId

data class TransferHistory(
    val transferDateTime: TransferDateTime,
    val userId: UserId,
    val transferStatus: TransferStatus,
    // TODO: Account 애그리거트의 ID VO객체를 참조하는 것이라 허용되는 패턴이지만, 타 애그리거트의 객체를 직접 참조하는 것이 불편하다면, 공유커널 패턴으로 사용하는 것을 검토햅로 수 있음. 그런데 공유커널 패턴이 오히려 공통지옥을 만들 수 있을 것 같음
    val accountNumber: Account
)
