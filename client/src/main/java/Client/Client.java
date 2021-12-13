package Client;

import packet.RequestPacket;
import parser.*;
import util.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private ExecutorService executorService;
    private SocketChannel socketChannel;
    private String userNick;
    private Map<UUID, ArrayList<byte[]>> responsePacketListMap = new ConcurrentHashMap<>();
    private Parser responseParser = new ResponseParser();
    private ByteBuffer readByteBuffer = ByteBuffer.allocateDirect(Constants.PACKET_TOTAL_SIZE);
    private ByteBuffer writeByteBuffer = ByteBuffer.allocateDirect(Constants.PACKET_TOTAL_SIZE);
    private int byteCount;

    public ExecutorService getExecutorService() {
        return this.executorService;
    }
    public SocketChannel getSocketChannel() {
        return this.socketChannel;
    }
    public String getUserNick() {
        return this.userNick;
    }
    public Map<UUID, ArrayList<byte[]>> getResponsePacketListMap() {
        return this.responsePacketListMap;
    }
    public ArrayList<byte[]> getResponsePacketList(UUID uuid) {
        return this.responsePacketListMap.get(uuid);
    }
    public Parser getResponseParser() {
        return this.responseParser;
    }
    public ByteBuffer getReadByteBuffer() { return this.readByteBuffer; }
    public ByteBuffer getWriteByteBuffer() { return this.writeByteBuffer; }
    public int getByteCount() { return this.byteCount; }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }
    public void setByteCount(int n) {this.byteCount = n;}
    public void initResponsePacketList(UUID uuid, ArrayList<byte[]> responsePacketList) {
        this.responsePacketListMap.put(uuid, responsePacketList);
    }
    public void clearResponsePacketList(UUID uuid) { this.responsePacketListMap.remove(uuid); }

    void startClient(Writer writer, String loginStr) {
        try {
            this.setExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            this.setSocketChannel(SocketChannel.open());
            this.getSocketChannel().configureBlocking(true);
            this.byteCount=0;
            String[] ipAndPortArray = loginStr.split(" ")[1].split(":");
            this.setUserNick(loginStr.split(" ")[2]);
            this.getSocketChannel().connect(new InetSocketAddress( ipAndPortArray[0], Integer.parseInt(ipAndPortArray[1])));
            System.out.println("서버 연결 완료");
            RequestPacket loginRequestPacket = new RequestPacket(
                    "Login",
                    this.getUserNick().getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
            writer.writeToChannel(loginRequestPacket.requestPacketList);
        } catch (IOException e) {
            System.out.println("startClient try-catch block IOException\n\n\n" + e + "\n\n\n");
            if (this.getSocketChannel().isOpen()) { stopClient(); }
            return;
        } catch (Exception e) {
            System.out.println("startClient try-catch block Exception\n\n\n" + e + "\n\n\n");
            if (this.getSocketChannel().isOpen()) { stopClient(); }
            return;
        }

        Reader reader = new Reader(this);
        reader.receive();
    }

    void stopClient() {
        try {
            if (this.getSocketChannel() != null && this.getSocketChannel().isOpen()) { this.getSocketChannel().close(); }
        } catch (IOException e) {
            System.out.println("stopClient IOException\n\n\n" + e + "\n\n\n");
        } catch (Exception e) {
            System.out.println("stopClient Exception\n\n\n" + e + "\n\n\n");
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        Scanner sc = new Scanner(System.in);
        SystemInputParser sip = new SystemInputParser(client, sc);

        sip.startClient();
    }
}
