package parser;

import util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ResponseParser implements Parser {
    @Override
    public byte[] getContents(ArrayList<byte[]> byteArrayList) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byteArrayList.size() * Constants.PACKET_CONTENTS_SIZE);
        int position = 0;
        for (byte[] byteArray : byteArrayList) {
            byte[] thisContentsLengthByteArray = Parser.sliceByteArray(
                    byteArray,
                    Constants.PACKET_LAST_FLAG_SIZE + Constants.PACKET_UUID_SIZE + Constants.PACKET_FUNCTION_SIZE,
                    Constants.PACKET_CONTENTS_LENGTH_SIZE);
            int thisContentsLength = Parser.byteArrayToInt(thisContentsLengthByteArray);
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

    @Override
    public byte[] getOptionalInfo(ArrayList<byte[]> byteArrayList) {
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

    @Override
    public ParsedMsg parseMessage(ArrayList<byte[]> byteArrayList) {
        byte[] totalOptionalInfo = this.getOptionalInfo(byteArrayList);
        int realOptionalInfoSize = totalOptionalInfo.length - (Constants.PACKET_TOTAL_PACKET_NUMBER_SIZE + Constants.PACKET_RESPONSE_CODE_SIZE);
        byte[] realOptionalInfo = Parser.sliceByteArray(totalOptionalInfo, (Constants.PACKET_TOTAL_PACKET_NUMBER_SIZE + Constants.PACKET_RESPONSE_CODE_SIZE), realOptionalInfoSize);
        int totalPacketNumber = Parser.byteArrayToInt(Parser.sliceByteArray(totalOptionalInfo, 0, Constants.PACKET_TOTAL_PACKET_NUMBER_SIZE));
        String status = Constants.getResponseStatus(totalOptionalInfo[Constants.PACKET_TOTAL_PACKET_NUMBER_SIZE]);

        return new ParsedMsg(
                Parser.getUUID(byteArrayList.get(0)),
                Parser.getFunctionName(byteArrayList.get(0)),
                this.getContents(byteArrayList),
                realOptionalInfo,
                totalPacketNumber,
                status
        );
    }
}
