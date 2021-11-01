package packet;


public interface ProtocolPacket {
    byte[] makePacketByteArray(int currentPacketNum);
    void makePacketList();
}
