package org.jnode.mail.service;

import java.io.IOException;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

public class EMailService {
	private String host;
	private String userName;
	private String passWord;
	private String fromAddr;
	private String port;

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public String getFromAddr() {
		return fromAddr;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}

	public void sendEMail(String emailTo, String subject, String text)
			throws Exception {
		String[] emailToA = { emailTo };
		sendEMail(emailToA, subject, text, null);
	}

	public void sendEMail(String[] emailTo, String subject, String text,
			String[] attachments) throws Exception {

		Session mailSession = getSession();
		String sendWarnings = new String();
		try {
			Transport transport = mailSession.getTransport();
			MimeMessage message = new MimeMessage(mailSession);
			message.setSubject(subject);
			message.setFrom(new InternetAddress(fromAddr));
			for (int i = 0; i < emailTo.length; i++) {
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(emailTo[i]));
			}
			MimeMultipart multiMessage = new MimeMultipart("related");
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(text, "text/plain; charset=utf-8");
			multiMessage.addBodyPart(textPart);
			if (attachments != null) {
				for (int i = 0; i < attachments.length; i++) {
					MimeBodyPart filePart = new MimeBodyPart();
					try {
						filePart.attachFile(attachments[i]);
						multiMessage.addBodyPart(filePart);
					} catch (IOException e) {
						sendWarnings += "Can not attach file " + attachments[i]
								+ "\n";
					}
				}
			}
			message.setContent(multiMessage);
			transport.connect();
			transport.sendMessage(message,
					message.getRecipients(Message.RecipientType.TO));
			if (sendWarnings.length() > 0) {
				throw new Exception(sendWarnings);
			}
		} catch (NoSuchProviderException e) {
			throw new Exception(e);
		} catch (MessagingException e) {
			throw new Exception(e);
		}

	}

	public void sendEMail(String emailTo, String subject, String text,
			String... attachments) throws Exception {
		String[] emailToA = { emailTo };
		sendEMail(emailToA, subject, text, attachments);

	}

	public void sendEMailMulti(String[] emailTo, String subject, String text,
			String... attachments) throws Exception {
		sendEMail(emailTo, subject, text, attachments);

	}

	private Session getSession() {
		Authenticator authenticator = new Authenticator();
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", host);
		props.setProperty("mail.port", port);
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.smtp.ssl.enable", "true");
		props.setProperty("mail.smtp.sasl.enable", "true");
		props.setProperty("mail.smtp.submitter", authenticator
				.getPasswordAuthentication().getUserName());
		return Session.getInstance(props, authenticator);
	}

	private class Authenticator extends javax.mail.Authenticator {
		private PasswordAuthentication authentication;

		public Authenticator() {
			String username = userName;
			String password = passWord;
			authentication = new PasswordAuthentication(username, password);
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}
}
