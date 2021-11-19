package server;

import parser.*;
import util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Client {
    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private Parser requestParser = new RequestParser();
    private String userNick;
    Map<UUID, ArrayList<byte[]>> requestPacketListMap = new HashMap<>();
    private Map<UUID, ArrayList<byte[]>> responsePacketListMap = new HashMap<>();

    public Client(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        this.selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        this.selectionKey.attach(this);
    }

    public SocketChannel getSocketChannel() { return this.socketChannel; }
    public SelectionKey getSelectionKey() { return this.selectionKey; }
    public Parser getRequestParser() { return this.requestParser; }
    public String getUserNick() {
        return this.userNick;
    }
    public Map<UUID, ArrayList<byte[]>> getResponsePacketListMap() { return this.responsePacketListMap; }
    public ArrayList<byte[]> getRequestPacketList(UUID uuid) {
        return this.requestPacketListMap.get(uuid);
    }
    public ArrayList<byte[]> getResponsePacketList(UUID uuid) {
        return this.responsePacketListMap.get(uuid);
    }

    public void setUserNick(String userNick) { this.userNick = userNick; }
    public void setResponsePacketList(UUID uuid, ArrayList<byte[]> responsePacketList) {
        this.responsePacketListMap.put(uuid, responsePacketList);
    }

    public void clearRequestPacketList(UUID uuid) { this.requestPacketListMap.remove(uuid); }
    public void clearResponsePacketList(UUID uuid) { this.responsePacketListMap.remove(uuid); }

//    public Runnable makeWholePacket() {
//        Runnable runnable = () -> {
//          try {
//              ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.PACKET_TOTAL_SIZE);
//              int byteCount = this.socketChannel.read(byteBuffer);
//
//              //상대방이 SocketChannel의 close() 메소드를 호출할 경우
//              if (byteCount == -1) {
//                  System.out.println("클라이언트 연결 정상적으로 끊김" + socketChannel.getRemoteAddress());
//                  Server.setClientList(false,this);
//                  return;
//              }
//
//              while ( 0 < byteCount && byteCount < Constants.PACKET_TOTAL_SIZE){
//                  byteCount += this.socketChannel.read(byteBuffer);
//              }
//
//              // 정상 동작 시작
//              byteBuffer.flip();
//              byte[] requestPacket = byteBuffer.array();
//              UUID uuid = Parser.getUUID(requestPacket);
//              if (!this.requestPacketListMap.containsKey(uuid)) {
//                  this.requestPacketListMap.put(uuid, new ArrayList<>());
//              }
//              this.requestPacketListMap.get(uuid).add(requestPacket);
//
//              if (!Parser.isLast(requestPacket)) {
//                  this.selectionKey.interestOps(SelectionKey.OP_READ);
//                  Server.getCallback().completed(null, null);
//              } else {
//                  Reader reader = new Reader(this);
//                  reader.deployWorker(uuid);
//              }
//          } catch (IOException e) {
//              System.out.println("server receive IOException\n\n\n");
//              e.printStackTrace();
//          }
//        };
//        return runnable;
//    }
}
