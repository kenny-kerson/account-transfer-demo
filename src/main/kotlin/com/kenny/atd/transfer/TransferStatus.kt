package com.kenny.atd.transfer

import com.kenny.atd.account.AccountStatus

enum class TransferStatus (
    val code: String,
) {
    IN_PROGRESS("00"),
    COMPLETED("01"),
    FAILED("09")
    ;

    companion object {
        fun fromCode(code: String): TransferStatus {
            return TransferStatus.entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown transfer status: $code")
        }
    }
}
