package com.rusudinu.backend.hash;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashService {
	public String shaHash(String data) {
		if (data == null || data.isEmpty()) {
			log.warn("Data is null or empty, returning empty hash");
			throw new IllegalArgumentException("Data cannot be null or empty");
		}
		return DigestUtils.sha256Hex(data);
	}

	@SneakyThrows
	public String hashDocument(byte[] document) {
		return DigestUtils.sha256Hex(document);
	}
}
