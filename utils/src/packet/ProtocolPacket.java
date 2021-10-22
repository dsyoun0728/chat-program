package packet;

import util.UniqueID;

public class ProtocolPacket {
    byte[] randomUniqueIDByteArray;
    int lastFlag;
    byte contentsLength;
    byte lastAndLength;
    byte[] contentsByteArray = new byte[80];
    byte[] optionalInfoByteArray = new byte[35];

    public ProtocolPacket(boolean isLast,  byte[] contents, byte[] optionalInfo) {
        this.randomUniqueIDByteArray = UniqueID.randomUniqueID();
        this.lastFlag = isLast ? 1 << 7: 0;
        this.contentsLength = (byte) contents.length;
        this.lastAndLength = (byte) (lastFlag + contentsLength);
        this.contentsByteArray = contents;
        this.optionalInfoByteArray = optionalInfo;
    }
}
