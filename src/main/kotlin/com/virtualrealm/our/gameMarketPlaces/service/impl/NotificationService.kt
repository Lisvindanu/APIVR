package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.Todo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class NotificationService {
    @Autowired
    private lateinit var emailSender: JavaMailSender

    fun scheduleReminder(todo: Todo) {
        todo.remindAt?.let { reminderTime ->
            // Implement scheduling logic here using Spring @Scheduled
            // atau gunakan library seperti Quartz
            sendReminderEmail(todo)
        }
    }

    private fun sendReminderEmail(todo: Todo) {
        todo.owner?.let { pengguna ->
            val message = SimpleMailMessage().apply {
                setTo(pengguna.email)
                setSubject("Reminder: ${todo.title}")
                setText("""
                    Halo ${pengguna.nama},
                    
                    Jangan lupa untuk menyelesaikan todo Anda:
                    ${todo.title}
                    
                    Due date: ${todo.dueDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
                    Priority: ${todo.priority}
                    
                    ${todo.description ?: ""}
                    
                    Salam,
                    Todo App
                """.trimIndent())
            }

            try {
                emailSender.send(message)
            } catch (e: Exception) {
                // Log error
                e.printStackTrace()
            }
        }
    }
}