/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.mail;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

/**
 *
 * @author junhwong
 */
public class SmtpSender {

    class MailAuthenticator extends Authenticator {

        private String username;
        private String password;

        /**
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        /**
         * @param username the username to set
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * @return the password
         */
        public String getPassword() {
            return password;
        }

        /**
         * @param password the password to set
         */
        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password.toCharArray());
        }
        
        public MailAuthenticator(String username,String passwd){
            this.username=username;
            this.password=passwd;
        }
    }
    
    private final transient Properties props = System.getProperties();
    private transient MailAuthenticator authenticator;
    
    public SmtpSender(String host,String username,String passwd){
        authenticator=new MailAuthenticator(username,passwd);
        
    }
    
}
