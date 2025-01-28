package com.virtualrealm.our.gameMarketPlaces.repository

import com.virtualrealm.our.gameMarketPlaces.entity.Pengguna
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PenggunaRepository : JpaRepository<Pengguna, Long> {
    fun findByEmail(email: String): Pengguna?
    fun findAllByEmailIn(emails: List<String>): List<Pengguna>
}