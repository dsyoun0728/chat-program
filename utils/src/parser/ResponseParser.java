package parser;

import util.Function;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ResponseParser implements Parser {

    @Override
    public boolean isLast(byte[] byteArray) { return byteArray[1] >> 7 == -1; }

    @Override
    public String getFunctionName(ArrayList<byte[]> byteArrayList) {
        return Function.getFuncionString(byteArrayList.get(0)[0]);
    }

    @Override
    public int getTotalPacketNumber(ArrayList<byte[]> byteArrayList) {
        return ByteBuffer.wrap(Parser.sliceByteArray(byteArrayList.get(0), 82,  4)).getInt();
    }

    @Override
    public byte[] getContents(ArrayList<byte[]> byteArrayList) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byteArrayList.size() * 80);
        int position = 0;
        for (byte[] byteArray : byteArrayList) {
            byte thisContentsLength = (byte) (byteArray[1] & 127);
            byteBuffer.put(Parser.sliceByteArray(byteArray, 2, thisContentsLength));
            if (byteBuffer.position() - position != thisContentsLength ) {
                throw new IOException("\n\npacket의 contents length 맞지 않음\n\n");
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
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byteArrayList.size() * 38);
        for (byte[] byteArray : byteArrayList) {
            byteBuffer.put(Parser.sliceByteArray(byteArray, 82, 38));
        }
        byteBuffer.flip();
        byte[] returnByteArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(returnByteArray);
        byteBuffer.clear();
        return returnByteArray;
    }
}
