package com.virtualrealm.our.gameMarketPlaces.service.impl

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender) {

    private val dotenv: Dotenv = Dotenv.load()

    fun sendOtp(email: String, otp: String) {
        val smtpUsername = dotenv["SMTP_USERNAME"]
        val smtpPassword = dotenv["SMTP_PASSWORD"] ?: "ynpg wwzi lenc lmze"
        val smtpHost = dotenv["SMTP_HOST"] ?: "smtp.gmail.com"
        val smtpPort = dotenv["SMTP_PORT"]?.toInt() ?: 587

        // Ensure that mailSender is configured properly
        val javaMailSender = mailSender as JavaMailSenderImpl
        javaMailSender.host = smtpHost
        javaMailSender.port = smtpPort
        javaMailSender.username = smtpUsername
        javaMailSender.password = smtpPassword

        val message = SimpleMailMessage()
        message.setTo(email)
        message.setSubject("Your OTP Code")
        message.setText("Your OTP code is: $otp. This code will expire in 5 minutes.")
        mailSender.send(message)
    }
}
