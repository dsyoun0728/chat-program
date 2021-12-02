package server;

import parser.*;
import util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private Parser requestParser = new RequestParser();
    private String userNick;
    private Map<UUID, ArrayList<byte[]>> requestPacketListMap = new ConcurrentHashMap<>();
    private Map<UUID, ArrayList<byte[]>> responsePacketListMap = new ConcurrentHashMap<>();
    private ByteBuffer readByteBuffer;
    private ByteBuffer writeByteBuffer;
    private int readCount;
    private int writeCount;

    public Client(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        this.selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        this.selectionKey.attach(this);
        this.readByteBuffer = ByteBuffer.allocateDirect(Constants.PACKET_TOTAL_SIZE);
        this.writeByteBuffer = ByteBuffer.allocateDirect(Constants.PACKET_TOTAL_SIZE);
        this.readCount = 0;
        this.writeCount = 0;
    }

    public SocketChannel getSocketChannel() { return this.socketChannel; }
    public Parser getRequestParser() { return this.requestParser; }
    public String getUserNick() { return this.userNick; }
    public Map<UUID, ArrayList<byte[]>> getRequestPacketListMap() { return this.requestPacketListMap; }
    public ArrayList<byte[]> getRequestPacketList(UUID uuid) {
        return this.requestPacketListMap.get(uuid);
    }
    public ByteBuffer getReadByteBuffer() { return this.readByteBuffer; }
    public ByteBuffer getWriteByteBuffer() { return this.writeByteBuffer; }
    public int getReadCount() { return this.readCount; }
    public int getWriteCount() { return this.writeCount; }
    public void setReadCount(int n) { this.readCount = n; }
    public void setWriteCount(int n) { this.writeCount = n; }
    public void setUserNick(String userNick) { this.userNick = userNick; }
    public void setResponsePacketList(UUID uuid, ArrayList<byte[]> responsePacketList) { this.responsePacketListMap.put(uuid, responsePacketList); };

    public void clearRequestPacketList(UUID uuid) { this.requestPacketListMap.remove(uuid); }
}
