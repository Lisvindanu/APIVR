package com.virtualrealm.our.gameMarketPlaces.repository

import com.virtualrealm.our.gameMarketPlaces.entity.OtpToken
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

interface OtpTokenRepository : JpaRepository<OtpToken, Long> {
    fun findByUserId(userId: Long): Optional<OtpToken>
    fun findByUserIdAndExpiresAtGreaterThan(userId: Long, now: LocalDateTime): OtpToken?
    fun findByUserIdAndOtp(userId: Long, otp: String): Optional<OtpToken>
    fun deleteByUserId(userId: Long)
}
