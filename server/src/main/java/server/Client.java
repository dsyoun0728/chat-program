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
    private int byteCount;
    private int sendCount;

    public Client(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        this.selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        this.selectionKey.attach(this);
        this.readByteBuffer = ByteBuffer.allocateDirect(Constants.PACKET_TOTAL_SIZE);
        this.writeByteBuffer = ByteBuffer.allocateDirect(Constants.PACKET_TOTAL_SIZE);
        this.byteCount = 0;
        this.sendCount = 0;
    }

    public SocketChannel getSocketChannel() { return this.socketChannel; }
    public SelectionKey getSelectionKey() { return this.selectionKey; }
    public Parser getRequestParser() { return this.requestParser; }
    public String getUserNick() { return this.userNick; }
    public Map<UUID, ArrayList<byte[]>> getRequestPacketListMap() { return this.requestPacketListMap; }
    public ArrayList<byte[]> getRequestPacketList(UUID uuid) {
        return this.requestPacketListMap.get(uuid);
    }
    public Map<UUID, ArrayList<byte[]>> getResponsePacketListMap() { return this.responsePacketListMap; }
    public ArrayList<byte[]> getResponsePacketList(UUID uuid) { return this.responsePacketListMap.get(uuid); }
    public ByteBuffer getReadByteBuffer() { return this.readByteBuffer; }
    public ByteBuffer getWriteByteBuffer() { return this.writeByteBuffer; }
    public int getByteCount() { return this.byteCount; }
    public int getSendCount() { return this.sendCount; }
    public void setByteCount(int s) { this.byteCount = s; }
    public void setSendCount(int s) { this.sendCount = s; }
    public void setUserNick(String userNick) { this.userNick = userNick; }
    public void setResponsePacketList(UUID uuid, ArrayList<byte[]> responsePacketList) { this.responsePacketListMap.put(uuid, responsePacketList); };

    public void clearRequestPacketList(UUID uuid) { this.requestPacketListMap.remove(uuid); }
    public void clearResponsePacketList(UUID uuid) { this.responsePacketListMap.remove(uuid); }
}
