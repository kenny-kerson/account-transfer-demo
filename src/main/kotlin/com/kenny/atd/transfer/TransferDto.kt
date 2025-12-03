package com.kenny.atd.transfer

object TransferDto {

    data class In(
        val fromAccountNumber: String,
        val toAccountNumber: String,
        val transferAmount: String,
    )

    data class Out(
        val fromAccountNumber: String,
        val toAccountNumber: String,
        val transferAmount: String,
    )
}