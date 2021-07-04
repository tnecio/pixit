package io.tnec.pixit.email

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component


@Component
class EmailService(@Autowired private val mailSender: JavaMailSender) {
    fun sendEmail(from: String, replyTo: String, to: String, subject: String, content: String) {
        val message = SimpleMailMessage()
        message.setFrom(from)
        message.setReplyTo(replyTo)
        message.setTo(to)
        message.setSubject(subject)
        message.setText(content)
        mailSender.send(message)
    }
}