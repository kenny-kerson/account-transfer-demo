package com.kenny.atd.transfer

object TransferDto {

    data class In(
        val account: String,
    )

    data class Out(
        val account: String,
    )
}