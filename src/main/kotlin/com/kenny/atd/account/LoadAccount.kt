package com.kenny.atd.account

import org.springframework.stereotype.Component

@Component
class LoadAccount(
    private val accountMainEntityRepository: AccountMainEntityRepository
) {
    fun execute(accountNumber: String): Account {
        return accountMainEntityRepository.findById(accountNumber).toDomain()
    }
}