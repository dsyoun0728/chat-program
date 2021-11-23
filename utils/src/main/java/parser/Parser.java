package parser;

import util.Constants;
import util.Function;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public interface Parser {
    // contents 가져오기
    byte[] getContents(ArrayList<byte[]> byteArrayList);

    // optionalInfo 확인하기
    byte[] getOptionalInfo(ArrayList<byte[]> byteArrayList);

    // parsedMsg를 반환하는 구현부분
    ParsedMsg parseMessage(ArrayList<byte[]> byteArrayList);

    // 마지막 packet임을 확인하는 부분
    static boolean isLast(byte[] byteArray) {
        return byteArray[0] == 1;
    }

    // packet 구분을 위한 UUID
    static UUID getUUID(byte[] byteArray) {
        byte[] uuidByteArray = sliceByteArray(byteArray, Constants.PACKET_LAST_FLAG_SIZE, Constants.PACKET_UUID_SIZE);
        ByteBuffer byteBuffer = ByteBuffer.wrap(uuidByteArray);
        Long high = byteBuffer.getLong();
        Long low = byteBuffer.getLong();

        return new UUID(high, low);
    }

    // function 확인을 위한 부분
    static String getFunctionName(byte[] byteArray) {
        byte[] functionNumByteArray = sliceByteArray(
                byteArray,
                Constants.PACKET_LAST_FLAG_SIZE + Constants.PACKET_UUID_SIZE,
                Constants.PACKET_FUNCTION_SIZE);
        return Function.getFuncionString(byteArrayToInt(functionNumByteArray));
    }

    // byteArray 중 4byte에 해당하는 것을 int로 바꿔주는 기능을 하는 메소드
    static  int byteArrayToInt(byte[] byteArr) {
        return ByteBuffer.wrap(byteArr).getInt();
     }

    // byteArray를 자르는 기능을 하는 메소드
    static byte[] sliceByteArray(byte[] byteArr, int offset, int length) {
        byte[] returnByteArray = new byte[length];
        for (int i=0; i< length; i++) {
            returnByteArray[i] = byteArr[offset + i];
        }
        return  returnByteArray;
    }
}
