package com.virtualrealm.our.gameMarketPlaces

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GamesStoreApplication

fun main(args: Array<String>) {
	// Check for missing keys and log a clear error message
	//test
	val requiredKeys = listOf(
		"STACKHERO_MARIADB_DATABASE_URL", "STACKHERO_MARIADB_HOST", "STACKHERO_MARIADB_PORT", "STACKHERO_MARIADB_ROOT_PASSWORD",
		"SMTP_HOST", "SMTP_PORT", "SMTP_USERNAME", "SMTP_PASSWORD",
		"GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET", "GOOGLE_REDIRECT_URI"
	)

	// Retrieve environment variables directly from the system
	val missingKeys = requiredKeys.filter { System.getenv(it).isNullOrBlank() }

	if (missingKeys.isNotEmpty()) {
		throw IllegalStateException("Missing required environment variables: ${missingKeys.joinToString()}")
	}

	// Now, set SMTP configuration properties in Spring Boot using system environment variables
	System.setProperty("spring.mail.host", System.getenv("SMTP_HOST") ?: "smtp.gmail.com")
	System.setProperty("spring.mail.port", System.getenv("SMTP_PORT") ?: "587")
	System.setProperty("spring.mail.username", System.getenv("SMTP_USERNAME") ?: "")
	System.setProperty("spring.mail.password", System.getenv("SMTP_PASSWORD") ?: "")
	System.setProperty("spring.mail.properties.mail.smtp.auth", "true")
	System.setProperty("spring.mail.properties.mail.smtp.starttls.enable", "true")

	// Running the application
	runApplication<GamesStoreApplication>(*args)
}
