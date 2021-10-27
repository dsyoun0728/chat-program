package packet;

public class ResponsePacket extends ProtocolPacket {
    byte responseCode;
    byte functionNum;
    public byte[] responsePacketByteArray = new byte[120];

    public ResponsePacket(byte responseCode, byte functionNum, boolean isLast, byte[] contents, byte[] optionalInfo) {
        super(isLast, contents, optionalInfo);
        this.responseCode = responseCode;
        this.functionNum = functionNum;
        makeResponsePacketByteArray();
    }

    public void makeResponsePacketByteArray() {
        int destPos = 0;

        // 0 ~ 2            unique id
        System.arraycopy(randomUniqueIDByteArray, 0, responsePacketByteArray, destPos, randomUniqueIDByteArray.length);
        destPos += randomUniqueIDByteArray.length;

        // 3                   response code
        responsePacketByteArray[destPos] = responseCode;
        destPos += 1;

        // 4                    last flag & contents length
        responsePacketByteArray[destPos] = lastAndLength;
        destPos += 1;

        // 5 ~ 84           contents
        System.arraycopy(contentsByteArray, 0, responsePacketByteArray, destPos, contentsByteArray.length);
        destPos += 80;

        // 85               optional information (functionNum)
        responsePacketByteArray[destPos] = functionNum;
        destPos += 1;

        // 86 ~ 119     optional information (etc)
        System.arraycopy(optionalInfoByteArray, 0, responsePacketByteArray, destPos, optionalInfoByteArray.length);
        destPos += 34;
    }
}
