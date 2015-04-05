/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author johnwhang
 */
public class SSLHelper {

    public static SSLContext getSSLContext() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        Properties p = new Properties();//Configuration.getConfig();
        String protocol = "TLSV1";//p.getProperty("protocol");
        String sCertificateFile = "D:\\server_rsa.key";//./certificate/server_rsa.key";//p.getProperty("serverCertificateFile");
        String sCertificatePwd = "123456";//p.getProperty("serverCertificatePwd");
        String sMainPwd ="123456";// p.getProperty("serverMainPwd");
        String cCertificateFile = "D:\\server_rsa.key";//p.getProperty("clientCertificateFile");
        String cCertificatePwd = "123456";//p.getProperty("clientCertificatePwd");
        String cMainPwd = "123456";//p.getProperty("clientMainPwd");

        //KeyStore class is used to save certificate.
        char[] c_pwd = sCertificatePwd.toCharArray();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(sCertificateFile), c_pwd);

        //KeyManagerFactory class is used to create KeyManager class.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        char[] m_pwd = sMainPwd.toCharArray();
        keyManagerFactory.init(keyStore, m_pwd);
	//KeyManager class is used to choose a certificate 
        //to prove the identity of the server side. 
        KeyManager[] kms = keyManagerFactory.getKeyManagers();

        TrustManager[] tms = null;
        if (false) {//客户端 p.getProperty("authority").equals("2")
            //KeyStore class is used to save certificate.
            c_pwd = cCertificatePwd.toCharArray();
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(cCertificateFile), c_pwd);

            //TrustManagerFactory class is used to create TrustManager class.
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            m_pwd = cMainPwd.toCharArray();
            trustManagerFactory.init(keyStore);
            //TrustManager class is used to decide weather to trust the certificate 
            //or not. 
            tms = trustManagerFactory.getTrustManagers();
        }

	//SSLContext class is used to set all the properties about secure communication.
        //Such as protocol type and so on.
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(kms, tms, null);

        return sslContext;
    }
    
    public static SSLContext createSSLContext(String protocol,String certFile,String passwd) throws GeneralSecurityException, FileNotFoundException, IOException{
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");
        char[] passphrase = passwd.toCharArray();
        ks.load(new FileInputStream(certFile), passphrase);
        ts.load(new FileInputStream(certFile), passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);
        SSLContext sslCtx = SSLContext.getInstance(protocol);//SSL
        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);//
        return sslCtx;
    }
    
    
    public static ServerSocketChannel getServerSocketChannel() throws Exception{
        SSLContext sslContext = getSSLContext();
        
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
        ServerSocketChannel c=sslServerSocket.getChannel();
        if(c==null){
            sslServerSocket.accept();
            
            throw new Exception("获取失败");
        }
        return c;
//        Socket ss=sslServerSocket.accept();
//        ss.getChannel().configureBlocking(true);
//        sslServerSocket.getChannel().accept();
//        
//        
    }
    
    
    public static boolean isEngineClosed(SSLEngine engine){
        return engine==null || (engine.isOutboundDone() && engine.isInboundDone());
    }
}
