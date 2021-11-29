package server;

import java.io.IOException;
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

    private String userNick;
    private Map<UUID, ArrayList<byte[]>> requestPacketListMap = new ConcurrentHashMap<>();

    public Client(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        this.selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        this.selectionKey.attach(this);
    }

    public SocketChannel getSocketChannel() { return this.socketChannel; }
    public String getUserNick() { return this.userNick; }
    public Map<UUID, ArrayList<byte[]>> getRequestPacketListMap() { return this.requestPacketListMap; }
    public ArrayList<byte[]> getRequestPacketList(UUID uuid) {
        return this.requestPacketListMap.get(uuid);
    }

    public void setUserNick(String userNick) { this.userNick = userNick; }

    public void clearRequestPacketList(UUID uuid) { this.requestPacketListMap.remove(uuid); }
}
