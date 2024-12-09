package com.virtualrealm.our.gameMarketPlaces.repository

import com.virtualrealm.our.gameMarketPlaces.entity.ApiKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


@EnableJpaRepositories
@SpringBootApplication
interface ApiKeyRepository: JpaRepository<ApiKey, String> {
}