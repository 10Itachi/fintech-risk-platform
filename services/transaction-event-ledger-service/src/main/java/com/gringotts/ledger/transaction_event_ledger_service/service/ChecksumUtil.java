package com.gringotts.ledger.transaction_event_ledger_service.service;

import org.apache.kafka.common.protocol.types.Field;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class ChecksumUtil {
    private ChecksumUtil(){}

    public static String sha256(String inPut){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(inPut.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex){
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
