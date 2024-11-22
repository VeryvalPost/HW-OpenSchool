package ru.t1.java.service2.util;

public class BlackListGenerator {
    /* Сделал тестовую сущность
    В случае если GlobalID клиента четный, то клиент не блокируется. Если нечетный, то блок.
     */

    public boolean generate(String globalClientID){
        if (globalClientID == null || globalClientID.isEmpty()) {
            throw new IllegalArgumentException("Global Client ID не доджен быть пуст");
        }
        char lastChar = globalClientID.charAt(globalClientID.length() - 1);

        if (!Character.isDigit(lastChar)) {
            throw new IllegalArgumentException("Global Client ID не заканчивается числом");
        }
        int lastDigit = Character.getNumericValue(lastChar);
        return lastDigit % 2 != 0;
    }
}
