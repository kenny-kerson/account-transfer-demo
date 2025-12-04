package com.kenny.atd.account

import org.springframework.stereotype.Repository

@Repository
class AccountMainEntityRepository {
    fun findById(accountNumber: String): AccountMainEntity? {
        return null;
    }
}