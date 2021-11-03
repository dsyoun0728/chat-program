package parser;

import java.io.IOException;
import java.util.ArrayList;

public interface Parser {

    // 마지막 packet임을 확인하는 부분
    boolean isLast(byte[] byteArray);

    // 기능 구분을 위한 부분
    String getFunctionName(ArrayList<byte[]> byteArrayList);

    // 총 Packet 개수를 확인하는 부분
    int getTotalPacketNumber(ArrayList<byte[]> byteArrayList);

    // 모든 body contents를 담는 부분
    byte[] getContents(ArrayList<byte[]> byteArrayList) throws IOException;

    // 추가적인 정보를 담는 부분
    byte[] getOptionalInfo(ArrayList<byte[]> byteArrayList);

    // response parser에서만 필요한 부분
    static String getStatus(ArrayList<byte[]> byteArrayList) {
        switch (byteArrayList.get(0)[86]) {
            case 20:
                return "success";
            default:
                return "no code";
        }
    }

    static byte[] sliceByteArray(byte[] byteArr, int offset, int length) {
        byte[] returnByteArray = new byte[length];
        for (int i=0; i< length; i++) {
            returnByteArray[i] = byteArr[offset + i];
        }
        return  returnByteArray;
    }
}
