package server.worker;

import parser.Parser;
import server.Client;
import util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.Callable;

public class CallableMaker implements Callable<Integer> {
    private ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.PACKET_TOTAL_SIZE);
    private Client client;
    private byte[] packet;
    private UUID uuid;
    private int byteCount;

    public CallableMaker(Client client, byte[] packet) {
        this.client = client;
        this.packet = packet;
        this.uuid = Parser.getUUID(this.packet);
        this.byteCount = 0;
    }

    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }

    @Override
    public Integer call() {
        byte[] restPacket = Parser.sliceByteArray(this.packet, this.byteCount, Constants.PACKET_TOTAL_SIZE - this.byteCount);
        this.byteBuffer.put(restPacket);
        this.byteBuffer.flip();
        try {
            this.byteCount += this.client.getSocketChannel().write(this.byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.byteBuffer.clear();
        return this.byteCount;
    }
}
