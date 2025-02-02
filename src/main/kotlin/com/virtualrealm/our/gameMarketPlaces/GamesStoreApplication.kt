package com.virtualrealm.our.gameMarketPlaces

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import io.github.cdimascio.dotenv.Dotenv

@SpringBootApplication(scanBasePackages = ["com.virtualrealm.our.gameMarketPlaces"])
class GameMarketPlacesApplication

@SpringBootApplication
class GamesStoreApplication

fun main(args: Array<String>) {
	// Load environment variables from .env file
	val dotenv = Dotenv.configure().load()

	// Check for missing keys and log a clear error message
	val requiredKeys = listOf(
		"STACKHERO_MARIADB_DATABASE_URL", "STACKHERO_MARIADB_ROOT_USERNAME", "STACKHERO_MARIADB_ROOT_PASSWORD",
		"SMTP_HOST", "SMTP_PORT", "SMTP_USERNAME", "SMTP_PASSWORD",
		"GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET", "GOOGLE_REDIRECT_URI", "SFTP_SERVER", "SFTP_PORT", "SFTP_USERNAME", "SFTP_PASSWORD",
		"YOUTUBE_API_KEY",
	)
	val missingKeys = requiredKeys.filter { dotenv[it].isNullOrBlank() }

	if (missingKeys.isNotEmpty()) {
		throw IllegalStateException("Missing required environment variables: ${missingKeys.joinToString()}")
	}

	// Set the environment variables to system properties
	requiredKeys.forEach { key ->
		System.setProperty(key, dotenv[key] ?: "")
	}

	// Now, set SMTP configuration properties in Spring Boot
	System.setProperty("spring.mail.host", dotenv["SMTP_HOST"] ?: "smtp.gmail.com")
	System.setProperty("spring.mail.port", dotenv["SMTP_PORT"] ?: "587")
	System.setProperty("spring.mail.username", dotenv["SMTP_USERNAME"] ?: "")
	System.setProperty("spring.mail.password", dotenv["SMTP_PASSWORD"] ?: "")
	System.setProperty("spring.mail.properties.mail.smtp.auth", "true")
	System.setProperty("spring.mail.properties.mail.smtp.starttls.enable", "true")

	System.setProperty("youtube.api.key", dotenv["YOUTUBE_API_KEY"] ?: "")


	runApplication<GamesStoreApplication>(*args)
}