package org.example.util;

public class MaskCardNumber {
    public String maskCardNumber(String cardNumber){
        if (cardNumber == null || cardNumber.length() < 4) {
            return "Invalid card number";
        }
        // Берём последние 4 цифры карты
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFourDigits;
    }
}
