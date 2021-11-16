package server;

import parser.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Client {
    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private Parser requestParser = new RequestParser();
    private String userNick;
    private ArrayList<byte[]> requestPacketList = new ArrayList<>();
    private ArrayList<byte[]> responsePacketList = new ArrayList<>();

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
    public ArrayList<byte[]> getRequestPacketList() {
        return this.requestPacketList;
    }
    public ArrayList<byte[]> getResponsePacketList() { return this.responsePacketList; }

    public void setUserNick(String userNick) { this.userNick = userNick; }
    public void setResponsePacketList(ArrayList<byte[]> responsePacketList) { this.responsePacketList = responsePacketList; }

    public void clearRequestPacketList() { this.requestPacketList.clear(); }
    public void clearResponsePacketList() { this.responsePacketList.clear(); }

    public Runnable makeWholePacket() {
        Runnable runnable = () -> {
          try {
              ByteBuffer byteBuffer = ByteBuffer.allocate(120);

              int byteCount = this.socketChannel.read(byteBuffer);


              //상대방이 SocketChannel의 close() 메소드를 호출할 경우
              if (byteCount == -1) {
                  System.out.println("클라이언트 연결 정상적으로 끊김" + socketChannel.getRemoteAddress());
                  Server.setClientList(false,this);
                  return;
              }

              while ( 0 < byteCount && byteCount < 120 ){
                  byteCount += this.socketChannel.read(byteBuffer);
              }

              // 정상 동작 시작
              byteBuffer.flip();
              byte[] requestPacket = byteBuffer.array();
              this.requestPacketList.add(requestPacket);

              if (!requestParser.isLast(requestPacket)) {
                  this.selectionKey.interestOps(SelectionKey.OP_READ);
                  Server.getCallback().completed(null, null);
              } else {
                  Reader reader = new Reader(this);
                  reader.deployWorker(this.requestParser.getFunctionName(this.requestPacketList));
              }
          } catch (IOException e) {
              System.out.println("server receive IOException\n\n\n");
              e.printStackTrace();
          }
        };
        return runnable;
    }
}
