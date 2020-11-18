package com.evolution.bootcamp.containers.integration

import java.util.Properties

import jakarta.mail.{Message, Session, Transport}
import jakarta.mail.internet.MimeMessage

import scala.util.Try

class MailAgent(smtpHost: String = "127.0.0.1", smtpPort: Int = 25) {

  def sendExampleEmail(subject: String, body: String) = {
    val props = new Properties()
    props.put("mail.smtp.host", smtpHost)
    props.put("mail.smtp.port", smtpPort)
    val session = Session.getInstance(props, null)

    Try {
      val msg = new MimeMessage(session)
      msg.setFrom("me@example.com")
      msg.setRecipients(Message.RecipientType.TO, "you@example.com")
      msg.setSubject(subject)
      msg.setText(s"$body\n")
      Transport.send(msg)
    }.toEither
  }

}
