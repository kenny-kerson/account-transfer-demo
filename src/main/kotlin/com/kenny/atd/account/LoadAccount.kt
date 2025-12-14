package com.kenny.atd.account

import com.kenny.atd.account.entity.Account
import com.kenny.atd.account.persistance.AccountMainEntityRepository
import com.kenny.atd.account.persistance.toDomain
import org.springframework.stereotype.Component

@Component
class LoadAccount(
    private val accountMainEntityRepository: AccountMainEntityRepository
) {
    fun execute(accountNumber: String): Account {
        return accountMainEntityRepository.findById(accountNumber).toDomain()
    }
}