package com.example.payments.adapter.controller

import com.example.payments.adapter.controller.model.TransferMoneyControllerRequest
import com.example.payments.adapter.controller.model.TransferMoneyControllerResponse
import com.example.payments.application.port.`in`.TransferMoneyOperation
import jakarta.validation.constraints.Positive
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/accounts")
@Validated
class AccountController(
    private val transferMoneyOperation: TransferMoneyOperation
) {

    @PostMapping(
        "/{id}/transfers",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun transfer(
        @PathVariable("id") @Positive id: Long,
        @Validated @RequestBody request: TransferMoneyControllerRequest
    ): Mono<TransferMoneyControllerResponse> {
        LOG.info("Call to POST /accounts/{}/transfers with request {}", id, request)
        return transferMoneyOperation.execute(request.toDomain(id))
            .map {
                val response = TransferMoneyControllerResponse(id, request.targetAccount, request.amount)
                LOG.info("Response to POST /accounts/{}/transfers: {}", id, response)
                response
            }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AccountController::class.java)
    }

}
