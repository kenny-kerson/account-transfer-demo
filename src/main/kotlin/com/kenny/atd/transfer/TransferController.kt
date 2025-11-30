package com.kenny.atd.transfer

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TransferController {
    private val logger = LoggerFactory.getLogger(TransferController::class.java)

    @PostMapping("/transfer")
    fun transfer( input: TransferDto.In ): TransferDto.Out {

        return TransferDto.Out( account = input.account)
    }
}