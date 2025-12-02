package com.kenny.atd.transfer

object ValidationTransferDto {
    data class In(
        val account: String,
    )

    data class Out(
        val account: String,
    )
}
