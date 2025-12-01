package com.kenny.atd.transfer

object ValidationTransferDto {
    data class In(
        val acocunt: String,
    )

    data class Out(
        val account: String,
    )
}
