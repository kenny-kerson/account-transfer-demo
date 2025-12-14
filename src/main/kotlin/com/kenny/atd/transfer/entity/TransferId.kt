package com.kenny.atd.transfer.entity

data class TransferId(
    val accountNumber: String,
    val transferDate: String,
    val transferSequenceNumber: Long,
)