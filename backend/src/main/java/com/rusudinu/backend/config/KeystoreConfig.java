package com.rusudinu.backend.config;

import lombok.SneakyThrows;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class KeystoreConfig {
	private final String KEYS_FOLDER = "keys/";

	@SneakyThrows
	@Bean
	public KeyStore ministryKeyStore() {
		Path keystorePath = Paths.get(KEYS_FOLDER, "ministry_key.jks");
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(keystorePath.toFile()), "ministry".toCharArray());
		return keyStore;
	}

	@SneakyThrows
	@Bean
	public PrivateKey ministryPrivateKey(KeyStore ministryKeyStore) {
		return (PrivateKey) ministryKeyStore.getKey("ministry_key", "ministry".toCharArray());
	}

	@SneakyThrows
	@Bean
	public PublicKey ministryPublicKey(KeyStore ministryKeyStore) {
		return ministryKeyStore.getCertificate("ministry_key").getPublicKey();
	}

	@SneakyThrows
	@Bean
	public MessageDigest messageDigest() {
		return MessageDigest.getInstance("SHA-256");
	}
}
