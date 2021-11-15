package packet;


public abstract class ProtocolPacket {
    byte functionNum;
    int contentsLength;
    byte[] contents;
    int totalPacketNum;
    byte[] totalPacketNumByteArray;
    byte[] optionalInfo;

    public ProtocolPacket(byte functionNum, byte[] contents) {
        this.functionNum = functionNum;
        this.contentsLength = contents.length;
        this.contents = contents;
        int tempPacketNum = (int) Math.ceil((double) this.contentsLength / 80);
        this.totalPacketNum = tempPacketNum == 0 ? 1 : tempPacketNum;
        this.totalPacketNumByteArray = intToByteArray(this.totalPacketNum);
        this.optionalInfo = new byte[this.totalPacketNum * 38];
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
}
