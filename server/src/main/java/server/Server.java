package server;

import parser.Parser;
import server.worker.Worker;
import util.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public ExecutorService executorService;
    private ServerSocketChannel serverSocketChannel;
    private static Selector selector;
    private static List<Client> clientList = new CopyOnWriteArrayList<>();
    private static List<String> fileList = new CopyOnWriteArrayList<>();
    public static Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();
    private static final CompletionHandler<Void, Void> callback = new CompletionHandler<Void, Void>() {
        @Override
        public void completed(Void unused, Void unused2) {
            selector.wakeup();
        }
        @Override
        public void failed(Throwable throwable, Void unused) {
            System.out.println(throwable.toString());
        }
    };
    boolean isQueueRun = false;


    public Server() {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            System.out.println("Server constructor try-catch block IOException\n\n\n");
            e.printStackTrace();
        }
    }

    public static List<Client> getClientList() { return clientList; }
    public static List<String> getFileList() { return fileList; }
    public static void setClientList(boolean add, Client client) {
        if (add) clientList.add(client);
        else clientList.remove(client);
    }
    public static void setFileList(boolean add, String fileName) {
        if (add) fileList.add(fileName);
        else fileList.remove(fileName);
    }

    public static CompletionHandler<Void, Void> getCallback() { return callback; }

    void startServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            // 서비스 포트 설정 및 논블로킹 모드로 설정
            serverSocketChannel.bind(new InetSocketAddress(5001));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("-------------------서버 접속 준비 완료-------------------");

        } catch (IOException e){
            System.out.println("startServer try-catch block IOException\n\n\n");
            e.printStackTrace();
            if (serverSocketChannel.isOpen()) { stopServer(); }
            return;
        }
        catch (Exception e) {
            System.out.println("startServer try-catch block Exception\n\n\n");
            e.printStackTrace();
            if (serverSocketChannel.isOpen()) { stopServer(); }
            return;
        }

        // 클라이언트 접속 시작
        while (true) {
            try {
                if(queue.peek() != null){
                    queue.poll().run();
                }
                    selector.selectNow();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();

                        if (selectionKey.isAcceptable()) {
                            accept(selectionKey);
                        } else if (selectionKey.isReadable()) {
                            selectionKey.interestOps(0);
                            Client client = (Client) selectionKey.attachment();
                            Runnable readRunnable = () -> {
                                try {
                                    ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.PACKET_TOTAL_SIZE);
                                    int byteCount = 0;
                                    byteCount = client.getSocketChannel().read(byteBuffer);
                                    //상대방이 SocketChannel의 close() 메소드를 호출할 경우
                                    if (byteCount == -1) {
                                        System.out.println("클라이언트 연결 정상적으로 끊김" + client.getSocketChannel().getRemoteAddress());
                                        Server.setClientList(false, client);
                                        return;
                                    }

                                    while (0 < byteCount && byteCount < Constants.PACKET_TOTAL_SIZE) {
                                        byteCount += client.getSocketChannel().read(byteBuffer);
                                    }

                                    // 정상 동작 시작
                                    byteBuffer.flip();
                                    byte[] requestPacket = byteBuffer.array();
                                    UUID uuid = Parser.getUUID(requestPacket);
                                    if (!client.getRequestPacketListMap().containsKey(uuid)) {
                                        client.getRequestPacketListMap().put(uuid, new ArrayList<>());
                                    }
                                    client.getRequestPacketList(uuid).add(requestPacket);
                                    if (Parser.isLast(requestPacket)) {
                                        Reader reader = new Reader(client);
                                        reader.deployWorker(uuid);
                                    } else {
                                        selectionKey.interestOps(SelectionKey.OP_READ);
                                        selector.wakeup();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            };
                            queue.offer(readRunnable);

                        } else if (selectionKey.isWritable()) {
                            selectionKey.interestOps(0);
                            Client client = (Client) selectionKey.attachment();
                            Runnable writeRunnable = () -> {
                                for (UUID uuid : client.getResponsePacketListMap().keySet()) {
                                    try {
                                        for (byte[] packet : client.getResponsePacketList(uuid)) {
                                            int byteCount = 0;
                                            ByteBuffer byteBuffer = ByteBuffer.wrap(packet);
                                            while (byteCount < Constants.PACKET_TOTAL_SIZE) {
                                                byteCount += client.getSocketChannel().write(byteBuffer);
                                            }
                                        }
                                    } catch (IOException e) {
                                        System.out.println("Writer IOException\t\t\t");
                                        Server.getCallback().failed(e, null);
                                        e.printStackTrace();
                                        Worker.handleClientOut(client, uuid);
                                    } catch (Exception e) {
                                        System.out.println("Writer Exception\n\n\n");
                                        Server.getCallback().failed(e, null);
                                        e.printStackTrace();
                                    }
                                    client.clearRequestPacketList(uuid);
                                    client.clearResponsePacketList(uuid);
                                }
                                selectionKey.interestOps(SelectionKey.OP_READ);
                                selector.wakeup();
                            };
                            queue.offer(writeRunnable);
                        }
                        iterator.remove();
                    }

            } catch (IOException e) {
                System.out.println("startServer runnable block IOException\n\n\n");
                e.printStackTrace();
                if (serverSocketChannel.isOpen()) { stopServer(); }
                break;
            } catch (Exception e) {
                System.out.println("startServer runnable block Exception\n\n\n");
                e.printStackTrace();
                if (serverSocketChannel.isOpen()) { stopServer(); }
                break;
            }
        }
    }

    // 연결된 모든 SocketChannel 닫기, ServerSocketChannel 닫기, Selector 닫기 코드 필요
    void stopServer() {
        try {
            Iterator<Client> iterator = clientList.iterator();
            while (iterator.hasNext()) {
                Client client = iterator.next();
                client.getSocketChannel().close();
                iterator.remove();
            }
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (executorService != null && !executorService.isShutdown()){
                executorService.shutdown();
            }
        } catch (IOException e) {
            System.out.println("stopServer IOException\n\n\n");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("stopServer Exception\n\n\n");
            e.printStackTrace();
        }
    }

    void accept(SelectionKey selectionKey) {
            try {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();

                String acceptInfo = "client-" + (clientList.size() + 1) + " connected : " + socketChannel.getRemoteAddress();
                System.out.println(acceptInfo);

                Client client = new Client(socketChannel, selector);
                clientList.add(client);
            } catch (IOException e) {
                System.out.println("accept IOException\n\n\n");
                e.printStackTrace();
                if (serverSocketChannel.isOpen()) {
                    stopServer();
                }
            } catch (Exception e) {
                System.out.println("accept Exception\n\n\n");
                e.printStackTrace();
                if (serverSocketChannel.isOpen()) {
                    stopServer();
                }
            }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}