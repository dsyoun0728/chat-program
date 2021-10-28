package util;

import java.util.HashSet;
import java.util.Random;

public class UniqueID {
    private static HashSet<String> uniqueIDSet = new HashSet<>();

    public static byte[] randomUniqueID() {
        int originalSetSize = uniqueIDSet.size();
        byte[] randomByteArray = new byte[3];
        new Random().nextBytes(randomByteArray);
        if (uniqueIDSet.size() == (2^24)) uniqueIDSet.removeAll(uniqueIDSet);
        uniqueIDSet.add(getFullUniqueID(randomByteArray));

        while (originalSetSize == uniqueIDSet.size()) {
            new Random().nextBytes(randomByteArray);
            uniqueIDSet.add(getFullUniqueID(randomByteArray));
        }

        return randomByteArray;
    }

    public static String getFullUniqueID(byte[] byteArr) {
        return  byteArr[0] + "_" + byteArr[1] +"_" + byteArr[2];
    }
}
