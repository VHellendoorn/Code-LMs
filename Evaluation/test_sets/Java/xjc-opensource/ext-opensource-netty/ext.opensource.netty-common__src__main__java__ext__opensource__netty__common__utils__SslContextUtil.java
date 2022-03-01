
package ext.opensource.netty.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.core.io.ClassPathResource;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * @author ben
 * @Title: basic
 * @Description:
 **/

public class SslContextUtil {

	public static SslContext createSSLServerContextForPKC(String sslFilePath,
			String sslPassword) {
		return createSSLServerContext(sslFilePath, sslPassword, "PKCS12",
				"SunX509");
	}

	public static SslContext createSSLClientContextForPKC(String sslFilePath,
			String sslPassword) {
		return createSSLClientContext(sslFilePath, sslPassword, "PKCS12",
				"SunX509");
	}

	public static SslContext createSSLServerContextForJKS(String sslFilePath,
			String sslPassword) {
		return createSSLServerContext(sslFilePath, sslPassword, "JKS",
				"SunX509");
	}

	public static SslContext createSSLClientContextForJKS(String sslFilePath,
			String sslPassword) {
		return createSSLClientContext(sslFilePath, sslPassword, "JKS",
				"SunX509");
	}

	public static SslContext createSSLClientContext(String sslFilePath,
			String sslPassword, String sslType, String algorithmName) {
		try {
			KeyStore ts = KeyStore.getInstance(sslType);
			TrustManagerFactory tf = TrustManagerFactory
					.getInstance(algorithmName);
			InputStream tsInputStream = new FileInputStream(
					getFileInputStream(sslFilePath));
			try {
				ts.load(tsInputStream, sslPassword.toCharArray());
				tf.init(ts);	
			} finally {
				tsInputStream.close();
			}
			
			//both-server
			KeyStore keyStore = KeyStore.getInstance(sslType);
			KeyManagerFactory kmf = KeyManagerFactory
					.getInstance(algorithmName);

			InputStream ksInputStream = new FileInputStream(
					getFileInputStream(sslFilePath));

			try {
				keyStore.load(ksInputStream, sslPassword.toCharArray());
				kmf.init(keyStore, sslPassword.toCharArray());

			} finally {
				ksInputStream.close();
			}
		
			return SslContextBuilder.forClient().trustManager(tf).keyManager(kmf).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static SslContext createSSLServerContext(String sslFilePath,
			String sslPassword, String sslType, String algorithmName) {
		try {
			KeyStore keyStore = KeyStore.getInstance(sslType);
			KeyManagerFactory kmf = KeyManagerFactory
					.getInstance(algorithmName);

			InputStream ksInputStream = new FileInputStream(
					getFileInputStream(sslFilePath));

			try {
				keyStore.load(ksInputStream, sslPassword.toCharArray());
				kmf.init(keyStore, sslPassword.toCharArray());

			} finally {
				ksInputStream.close();
			}
			
            ///both-client
			KeyStore ts = KeyStore.getInstance(sslType);
			TrustManagerFactory tf = TrustManagerFactory
					.getInstance(algorithmName);

			InputStream tsInputStreamx = new FileInputStream(
					getFileInputStream(sslFilePath));
			try {

				ts.load(tsInputStreamx, sslPassword.toCharArray());
				tf.init(ts);
			} finally {
				tsInputStreamx.close();
			}
			return SslContextBuilder.forServer(kmf).trustManager(tf).build();

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static File getFileInputStream(String fileName) {
		File sFile = null;
		sFile = new File(fileName);
		boolean bExists = false;

		if (!sFile.exists()) {
			ClassPathResource res = new ClassPathResource(fileName);
			if (res.exists()) {
				bExists = true;
				try {
					sFile = res.getFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			bExists = true;
		}

		if (!bExists) {
			throw new RuntimeException("file not exist: " + fileName);
		}
		return sFile;
	}
}
