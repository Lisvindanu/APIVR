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
        val smtpPassword = dotenv["SMTP_PASSWORD"]
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

    fun sendPasswordResetLink(email: String, token: String) {
        val smtpUsername = dotenv["SMTP_USERNAME"] ?: "retrogamea00@gmail.com"
        val smtpPassword = dotenv["SMTP_PASSWORD"] ?: "ynbk xvjc paed szwv"
        val smtpHost = dotenv["SMTP_HOST"] ?: "smtp.gmail.com"
        val smtpPort = dotenv["SMTP_PORT"]?.toInt() ?: 587
        // Pastikan mailSender sudah dikonfigurasi dengan benar
        val javaMailSender = mailSender as JavaMailSenderImpl
        javaMailSender.host = smtpHost
        javaMailSender.port = smtpPort
        javaMailSender.username = smtpUsername
        javaMailSender.password = smtpPassword

        val resetLink = "http://127.0.0.1:8000/show-reset-password/$token"

        val message = SimpleMailMessage()
        message.setTo(email)
        message.setSubject("Password Reset Request")
        message.setText("We received a request to reset your password. " +
                "Click the link below to reset your password:\n$resetLink\n" +
                "This link will expire in 1 hour.")
        mailSender.send(message)
    }
}
