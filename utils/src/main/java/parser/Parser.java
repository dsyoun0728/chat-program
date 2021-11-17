package parser;

import util.Constants;
import util.Function;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public interface Parser {
    // parsedMsg를 반환하는 구현부분
    ParsedMsg parseMessage(ArrayList<byte[]> byteArrayList);

    // 마지막 packet임을 확인하는 부분
    public static boolean isLast(byte[] byteArray) {
        return byteArray[0] == 1;
    }

    // packet 구분을 위한 UUID
    public static UUID getUUID(byte[] byteArray) {
        byte[] uuidByteArray = sliceByteArray(byteArray, Constants.PACKET_LAST_FLAG_SIZE, Constants.PACKET_UUID_SIZE);
        ByteBuffer byteBuffer = ByteBuffer.wrap(uuidByteArray);
        Long high = byteBuffer.getLong();
        Long low = byteBuffer.getLong();

        return new UUID(high, low);
    }

    // function 확인을 위한 부분
    public static String getFunctionName(byte[] byteArray) {
        byte[] functionNumByteArray = sliceByteArray(
                byteArray,
                Constants.PACKET_LAST_FLAG_SIZE + Constants.PACKET_UUID_SIZE,
                Constants.PACKET_FUNCTION_SIZE);
        return Function.getFuncionString(byteArrayToInt(functionNumByteArray));
    }

    // contents 가져오기
    public static byte[] getContents(ArrayList<byte[]> byteArrayList){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byteArrayList.size() * Constants.PACKET_CONTENTS_SIZE);
        int position = 0;
        for (byte[] byteArray : byteArrayList) {
            byte[] thisContentsLengthByteArray = sliceByteArray(
                    byteArray,
                    Constants.PACKET_LAST_FLAG_SIZE + Constants.PACKET_UUID_SIZE + Constants.PACKET_FUNCTION_SIZE,
                    Constants.PACKET_CONTENTS_LENGTH_SIZE);
            int thisContentsLength = byteArrayToInt(thisContentsLengthByteArray);
            byteBuffer.put(Parser.sliceByteArray(
                    byteArray,
                    Constants.PACKET_LAST_FLAG_SIZE + Constants.PACKET_UUID_SIZE + Constants.PACKET_FUNCTION_SIZE + Constants.PACKET_CONTENTS_LENGTH_SIZE,
                    thisContentsLength));
            if (byteBuffer.position() - position != thisContentsLength ) {
                try {
                    throw new IOException("\n\npacket의 contents length 맞지 않음\n\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            position = byteBuffer.position();
        }
        byteBuffer.flip();
        byte[] returnByteArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(returnByteArray);
        byteBuffer.clear();
        return returnByteArray;
    }

    // optionalInfo 확인하기
    public static byte[] getOptionalInfo(ArrayList<byte[]> byteArrayList) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byteArrayList.size() * Constants.PACKET_OPTIONAL_INFO_SIZE);
        for (byte[] byteArray : byteArrayList) {
            byteBuffer.put(Parser.sliceByteArray(
                    byteArray,
                    Constants.PACKET_LAST_FLAG_SIZE + Constants.PACKET_UUID_SIZE + Constants.PACKET_FUNCTION_SIZE + Constants.PACKET_CONTENTS_LENGTH_SIZE + Constants.PACKET_CONTENTS_SIZE,
                    Constants.PACKET_OPTIONAL_INFO_SIZE));
        }
        byteBuffer.flip();
        byte[] returnByteArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(returnByteArray);
        byteBuffer.clear();
        return returnByteArray;
    }

    // total packet number 확인하기
    public static  int getTotalPacketNumber(byte[] byteArray) {
        return byteArrayToInt(
                sliceByteArray(byteArray, 0, Constants.PACKET_TOTAL_PACKET_NUMBER_SIZE)
        );
    }

    // byteArray 중 4byte에 해당하는 것을 int로 바꿔주는 기능을 하는 메소드
     public static  int byteArrayToInt(byte[] byteArr) {
        return ByteBuffer.wrap(byteArr).getInt();
     }

    // byteArray를 자르는 기능을 하는 메소드
    public static byte[] sliceByteArray(byte[] byteArr, int offset, int length) {
        byte[] returnByteArray = new byte[length];
        for (int i=0; i< length; i++) {
            returnByteArray[i] = byteArr[offset + i];
        }
        return  returnByteArray;
    }
}
