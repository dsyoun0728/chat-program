package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class UniqueID {
    private static HashSet<String> uniqueIDSet = new HashSet<>();
    private static HashMap<String, HashSet<String>> uniqueIDMap = new HashMap<>();

    public static byte[] randomUniqueID() {
        int originalSetSize = uniqueIDSet.size();

        // 3 byte의 개수가 넘어가면 Set 내부에 있는 데이터 모두 삭제하고 다시 새롭게 집어넣을 것
        if (uniqueIDSet.size() == (2^24)) uniqueIDSet.removeAll(uniqueIDSet);

        byte[] randomByteArray = new byte[3];
        new Random().nextBytes(randomByteArray);

        uniqueIDSet.add(getFullUniqueID(randomByteArray));

        // 동일한 random ID가 나오는 경우를 방지하기 위한 부분
        while (originalSetSize == uniqueIDSet.size()) {
            new Random().nextBytes(randomByteArray);
            uniqueIDSet.add(getFullUniqueID(randomByteArray));
        }

        return randomByteArray;
    }

    public static String getFullUniqueID(byte[] byteArr) {
        String returnStr = "";
        for (byte byteMember:byteArr) {
            // byteArr에 들어온 각각의 값이 byte가 아니라면 최종 returnStr의 길이가 12가 될 수 없도록 하기 위함
            if (byteMember <= Byte.MAX_VALUE && byteMember >= Byte.MIN_VALUE) {
                returnStr += String.format("%4d", byteMember);          // 12자리로 고정하기 위함
            }
        }
        return returnStr;
    }

    // Server에서만 쓰임
    public static boolean isUnique(String uniqueIDString, String userNick) {
        // Server의 uniqueIDSet에 등록되어 있는 경우 false를 리턴해 분기처리
        // 동일한 클라이언트에서 온 경우는 기존 data에 연결하도록 하고
        // 다른 클라이언트에서 온 경우는 responseCode에 동일한 uniqueID임을 알려주어 새로운 uniqueID로 request하도록 유도

        HashSet<String> userUniqueIDSet = uniqueIDMap.get(userNick);
        int originalSetSize = userUniqueIDSet.size();

        // 3 byte의 개수가 넘어가면 Set 내부에 있는 데이터 모두 삭제하고 다시 새롭게 집어넣을 것
        if (userUniqueIDSet.size() == (2^24)) uniqueIDMap.get(userNick).removeAll(uniqueIDMap.get(userNick));

        userUniqueIDSet.add(uniqueIDString);

        return originalSetSize != userUniqueIDSet.size();
    }
}
