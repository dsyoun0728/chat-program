package util;

import java.util.Random;

public class UniqueID {
    public static byte[] randomUniqueID() {
        byte[] randomByteArray = new byte[3];
        new Random().nextBytes(randomByteArray);
        return randomByteArray;
    }

    public static String getFullUniqueID(byte[] byteArr) {
        return  byteArr[0] + "_" + byteArr[1] +"_" + byteArr[2];
    }
}
