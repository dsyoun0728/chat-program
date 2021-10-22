package packet;

import packet.ProtocolPacket;
import util.Function;

public class RequestPacket extends ProtocolPacket {
    byte functionNum;
    byte[] requestPacketByteArray = new byte[120];

    public RequestPacket(String functionName, boolean isLast,  byte[] contents, byte[] optionalInfo) {
        super(isLast, contents, optionalInfo);
        this.functionNum = Function.getFunctionByte(functionName);
        makeRequestPacketByteArray();
    }

    public void makeRequestPacketByteArray() {
        int destPos = 0;

        // 0 ~ 2            unique id
        System.arraycopy(randomUniqueIDByteArray, 0, requestPacketByteArray, destPos, randomUniqueIDByteArray.length);
        destPos += randomUniqueIDByteArray.length;

        // 3                    function 정보
        requestPacketByteArray[destPos] = functionNum;
        destPos += 1;

        // 4                    last flag & contents length
        requestPacketByteArray[destPos] = lastAndLength;
        destPos += 1;

        // 5 ~ 84           contents
        System.arraycopy(contentsByteArray, 0, requestPacketByteArray, destPos, contentsByteArray.length);
        destPos += 80;

        // 85 ~ 119       optional information
        System.arraycopy(optionalInfoByteArray, 0, requestPacketByteArray, destPos, optionalInfoByteArray.length);
        destPos += 35;
    }
}
