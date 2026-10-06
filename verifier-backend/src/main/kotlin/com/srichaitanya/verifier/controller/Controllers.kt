package com.srichaitanya.verifier.controller

import com.srichaitanya.verifier.dto.VerificationHistoryDto
import com.srichaitanya.verifier.dto.VerificationResultDto
import com.srichaitanya.verifier.dto.VerifyPresentationRequest
import com.srichaitanya.verifier.service.VerificationEngineService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/verification")
@Tag(name = "Verification", description = "QR Presentation and Digital Certificate Verification Engine")
class VerificationController(
    private val verificationEngineService: VerificationEngineService
) {
    @PostMapping("/verify")
    @Operation(summary = "Verify signed QR presentation payload (signature, issuer, expiry, revocation, nonce)")
    fun verify(@Valid @RequestBody request: VerifyPresentationRequest): ResponseEntity<VerificationResultDto> {
        return ResponseEntity.ok(verificationEngineService.verifyPresentation(request))
    }

    @GetMapping("/history")
    @Operation(summary = "Retrieve verifier audit trail and recent verification events")
    fun getHistory(): ResponseEntity<List<VerificationHistoryDto>> {
        return ResponseEntity.ok(verificationEngineService.getHistory())
    }
}
