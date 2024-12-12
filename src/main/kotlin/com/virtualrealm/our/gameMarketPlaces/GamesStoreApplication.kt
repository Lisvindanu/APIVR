package com.virtualrealm.our.gameMarketPlaces

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import io.github.cdimascio.dotenv.Dotenv

@SpringBootApplication
class GamesStoreApplication

fun main(args: Array<String>) {
	// Load environment variables from .env file
	val dotenv = Dotenv.configure().load()

	// Check for missing keys and log a clear error message
	val requiredKeys = listOf(
		"DATABASE_URL", "DATABASE_USERNAME", "DATABASE_PASSWORD",
		"SMTP_HOST", "SMTP_PORT", "SMTP_USERNAME", "SMTP_PASSWORD",
		"GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET", "GOOGLE_REDIRECT_URI"
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

	runApplication<GamesStoreApplication>(*args)
}