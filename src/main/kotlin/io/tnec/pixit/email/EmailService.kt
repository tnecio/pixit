package io.tnec.pixit.email

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class EmailService(@Autowired private val mailSender: JavaMailSender) {
    val EMAIL_ADDRESS_PATTERN =
            Pattern.compile(
                    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                            "\\@" +
                            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                            "(" +
                            "\\." +
                            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                            ")+"
            )

    private fun isValidEmailAddress(str: String): Boolean =
            EMAIL_ADDRESS_PATTERN.matcher(str).matches() && !str.isBlank()

    fun sendEmail(from: String, replyTo: String, to: String, subject: String, content: String) {
        val message = SimpleMailMessage()
        message.setFrom(from)
        if (isValidEmailAddress(replyTo)) {
            message.setReplyTo(replyTo)
            message.setText(content)
        } else {
            message.setReplyTo(from)
            message.setText("From: " + replyTo + "\n Content: " + content)
        }
        message.setTo(to)
        message.setSubject(subject)
        mailSender.send(message)
    }
}
