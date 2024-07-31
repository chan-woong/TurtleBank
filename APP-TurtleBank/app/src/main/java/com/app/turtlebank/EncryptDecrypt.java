package com.app.turtlebank;
// 암호화

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;

public class EncryptDecrypt {
    static public String secret = "amazing";
    static public int secretLength = secret.length();

    public static String operate(String input) {
        String result = "";
        for(int i = 0; i < input.length(); i++) {
            int xorVal = (int) input.charAt(i) ^ (int) secret.charAt(i % secretLength);
            char xorChar =  (char) xorVal;

            result += xorChar;
        }

        return result;
    }

    public static String encrypt(String input) {
        // base64
        String encVal = operate(input);
        String val = Base64.encodeToString(encVal.getBytes(),0);
        return val;
    }

    public static String decrypt(String input) {
        // 디코딩 byte
        try {
            // 디코딩된 바이트
            byte[] decodeByte = Base64.decode(input, Base64.DEFAULT);
            String decodeString = new String(decodeByte, StandardCharsets.UTF_8);
            String decryptString = operate(decodeString);

            return decryptString;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e("ENCRYPT_DECRYPT", "Base64 decoding error: " + e.getMessage());
            return "";  // 적절하게 오류를 처리하세요. 이 부분은 임시적인 예시입니다.
        }
    }
}
