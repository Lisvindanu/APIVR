package com.virtualrealm.our.gameMarketPlaces.repository

import com.virtualrealm.our.gameMarketPlaces.entity.User
import com.virtualrealm.our.gameMarketPlaces.entity.UserToken
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository: JpaRepository<UserToken,Long> {
    fun findByToken(token: String): UserToken?
    fun findBySub(sub : String): UserToken?
    fun deleteByToken(token: String)
    fun findByUser(user: User): UserToken?
    fun deleteAllByUserId(userId: Long)
}