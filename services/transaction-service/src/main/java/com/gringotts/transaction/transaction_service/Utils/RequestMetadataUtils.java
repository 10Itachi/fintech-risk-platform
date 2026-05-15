package com.gringotts.transaction.transaction_service.Utils;

import com.gringotts.enums.Channel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestMetadataUtils {

        public static String getDeviceId(Channel channel) {
            HttpServletRequest request = getRequest();

            // 1. Try Header first (Safe for testing)
            String headerId = (request != null) ? request.getHeader("X-Device-ID") : null;
            if (headerId != null && headerId.matches("^DEV-(ANDROID|IOS|WEB)-[A-Z0-9]{5,20}$")) {
                return headerId;
            }

            // 2. Map Channel to the required DEV- pattern
            return switch (channel) {
                case CARD -> "DEV-WEB-ATM001"; // Fixed format for ATM
                case UPI -> "DEV-ANDROID-MOBILE"; // Simulated Mobile
                case NET_BANKING -> "DEV-WEB-LAPTOP"; // Simulated Web
                default -> "DEV-WEB-SYSTEM";
            };
        }

        public static String getCountry() {
            HttpServletRequest request = getRequest();
            String country = "IN"; // Default

            if (request != null) {
                String awsCountry = request.getHeader("CloudFront-Viewer-Country");
                if (awsCountry != null && awsCountry.length() <= 3) {
                    country = awsCountry.toUpperCase();
                }
            }
            return country; // Returns "IN", which satisfies ^[A-Z]{2,3}$
        }
        private static HttpServletRequest getRequest() {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return (attrs != null) ? attrs.getRequest() : null;
        }
    }

