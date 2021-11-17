package packet;

import util.Constants;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class ProtocolPacket {
    byte[] UUIDByteArr;
    byte[] function;
    int totalContentsLength;
    byte[] contents;
    int totalPacketNum;
    byte[] totalPacketNumByteArray;
    byte[] optionalInfo;

    public ProtocolPacket(UUID uuid, int functionNum, byte[] contents) {
        this.UUIDByteArr = getByteArrayFromUUID(uuid);
        this.function = intToByteArray(functionNum);
        this.totalContentsLength = contents.length;
        this.contents = contents;
        int tempPacketNum = (int) Math.ceil((double) this.totalContentsLength / Constants.PACKET_CONTENTS_SIZE);
        this.totalPacketNum = tempPacketNum == 0 ? 1 : tempPacketNum;
        this.totalPacketNumByteArray = intToByteArray(this.totalPacketNum);
        this.optionalInfo = new byte[this.totalPacketNum * Constants.PACKET_OPTIONAL_INFO_SIZE];
    }

    abstract byte[] makePacketByteArray(int currentPacketNum);
    abstract void makePacketList();

    public byte[] intToByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) (value >> 24);
        byteArray[1] = (byte) (value >> 16);
        byteArray[2] = (byte) (value >> 8);
        byteArray[3] = (byte) (value);
        return  byteArray;
    }

    private byte[] getByteArrayFromUUID(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());

        return byteBuffer.array();
    }
}
