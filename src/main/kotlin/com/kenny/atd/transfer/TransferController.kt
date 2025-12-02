package com.kenny.atd.transfer

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TransferController(
    private val transferService: TransferService
) {
    private val logger = LoggerFactory.getLogger(TransferController::class.java)

    @PostMapping("/transfer")
    fun transfer( input: TransferDto.In ): TransferDto.Out {
        transferService.transfer();
        return TransferDto.Out( account = input.account )
    }

    @PostMapping( "/transfer/validate")
    fun validateTransfer( input: ValidationTransferDto.In ): ValidationTransferDto.Out {

        return ValidationTransferDto.Out( account = input.account )
    }
}