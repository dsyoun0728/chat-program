package server.worker;

import server.Client;
import server.Server;
import util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class RunnableMaker implements Runnable{
    private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(Constants.PACKET_TOTAL_SIZE * Constants.NUMBER_OF_PACKETS);
    private boolean isFirst = true;
    private boolean isLast = false;
    private Client client;
    private ArrayList<byte[]> packetList;

    public RunnableMaker(Client client, ArrayList<byte[]> packetList) {
        this.client = client;
        this.packetList = packetList;
    }

    @Override
    public void run() {
        // byteBuffer를 채우는 부분
        int bufferSize = Math.min(Constants.NUMBER_OF_PACKETS, this.packetList.size());
        byte[] toBuffer = new byte[Constants.PACKET_TOTAL_SIZE * bufferSize];
        if (this.isFirst) {
            if (this.packetList.size() <= Constants.NUMBER_OF_PACKETS) {
                for (int i = 0; i < this.packetList.size(); i++) {
                    System.arraycopy(packetList.get(i), 0, toBuffer, i * Constants.PACKET_TOTAL_SIZE, Constants.PACKET_TOTAL_SIZE);
                }
                this.packetList.clear();
                this.isLast = true;
            } else {
                for (int i=0; i < Constants.NUMBER_OF_PACKETS; i++) {
                    System.arraycopy(this.packetList.get(0), 0, toBuffer, i * Constants.PACKET_TOTAL_SIZE, Constants.PACKET_TOTAL_SIZE);
                    this.packetList.remove(0);
                }
            }
            this.isFirst = false;
        } else {
            // remaining()이 0인 경우는 byteBuffer에 있는 모든 것들을 write했다는 뜻
            if (this.byteBuffer.remaining() == 0 && !this.isLast) {
                int currentPacketListSize = this.packetList.size();
                int iterationNum = Math.min(currentPacketListSize, Constants.NUMBER_OF_PACKETS);
                for (int i=0; i < iterationNum; i++) {
                    System.arraycopy(this.packetList.get(0), 0, toBuffer, i * Constants.PACKET_TOTAL_SIZE, Constants.PACKET_TOTAL_SIZE);
                    this.packetList.remove(0);
                }
                this.isLast = this.packetList.isEmpty();
                this.byteBuffer.clear();
            }
        }
        if (this.byteBuffer.position() == 0) {
            this.byteBuffer.put(toBuffer);
            this.byteBuffer.flip();
        }

        // channel에 write하는 부분
        try {
            int ddd = this.client.getSocketChannel().write(this.byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 아직 해당 buffer에 있는걸 다 보내지 못하거나 보내야할 packet이 남아있다면 다시 호출해야함
        if (this.byteBuffer.remaining() !=0 || !this.isLast) {
            Server.getQueue().offer(this);
            Server.getSelector().wakeup();
        }
    }
}
