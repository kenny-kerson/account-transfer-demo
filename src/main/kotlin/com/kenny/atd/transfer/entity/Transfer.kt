package com.kenny.atd.transfer.entity

import com.kenny.atd.account.vo.AccountId
import com.kenny.atd.shared.Money
import com.kenny.atd.transfer.vo.TransferId
import org.slf4j.LoggerFactory

class Transfer(
    val id: TransferId,
    val fromAccount: AccountId,
    val toAccount: AccountId,
    val amount: Money,
    ) {}