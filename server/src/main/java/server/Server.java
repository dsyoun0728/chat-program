package server;

import parser.Parser;
import parser.RequestParser;
import util.Constants;

import java.io.File;
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
    private ExecutorService executorService;
    private ServerSocketChannel serverSocketChannel;
    private static Parser requestParser = new RequestParser();
    private static Selector selector;
    private static List<Client> clientList = new CopyOnWriteArrayList<>();
    private static List<String> fileList = new CopyOnWriteArrayList<>();
    private static Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();

    public Server() {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            System.out.println("Server constructor try-catch block IOException\n\n\n");
            e.printStackTrace();
        }
    }

    public static Parser getRequestParser() { return requestParser; }

    public static Selector getSelector() { return selector; }

    public static List<Client> getClientList() { return clientList; }
    public static List<String> getFileList() { return fileList; }
    public static Queue<Runnable> getQueue() { return queue; }
    public static void setClientList(boolean add, Client client) {
        if (add) clientList.add(client);
        else clientList.remove(client);
    }
    public static void setFileList(boolean add, String fileName) {
        if (add) fileList.add(fileName);
        else fileList.remove(fileName);
    }

    void startServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            // 서비스 포트 설정 및 논블로킹 모드로 설정
            serverSocketChannel.bind(new InetSocketAddress(5001));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            File files[] = new File("../chat-program-data").listFiles();
            for (File file : files) {
                setFileList(true, file.getName());
            }

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
                if (queue.isEmpty() && queue.peek() == null) selector.select(10000);
                else {
                    selector.selectNow();
                    queue.poll().run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    if (selectionKey.isAcceptable()) {
                        accept(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        Client client = (Client) selectionKey.attachment();
                        int byteCount = 0;
                        try {
                            byteCount = client.getSocketChannel().read(client.getReadBuffer());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        // 상대방이 SocketChannel의 close() 메소드를 호출할 경우
                        if (byteCount == -1) {
                            System.out.println("클라이언트 연결 정상적으로 끊김" + client.getSocketChannel().getRemoteAddress());
                            Server.setClientList(false, client);
                            if (client.getSocketChannel() != null && client.getSocketChannel().isOpen()) client.getSocketChannel().close();
                            return;
                        }

                        if (client.getReadBuffer().position() >= Constants.PACKET_TOTAL_SIZE) {
                            // 여기까지 왔다는건 buffer가 packet size 만큼 찼다는 것
                            client.getReadBuffer().flip();
                            byte[] requestPacket = new byte[Constants.PACKET_TOTAL_SIZE];
                            client.getReadBuffer().get(requestPacket, 0, Constants.PACKET_TOTAL_SIZE);
                            client.getReadBuffer().compact();

                            // client에 있는 packetList에 현재 완성된 packet 넣어주는 작업
                            UUID uuid = Parser.getUUID(requestPacket);
                            if (!client.getRequestPacketListMap().containsKey(uuid)) {
                                client.getRequestPacketListMap().put(uuid, new ArrayList<>());
                            }
                            client.getRequestPacketList(uuid).add(requestPacket);

                            // Worker Thread에 맡길 부분
                            if (Parser.isLast(requestPacket)) {
                                Runnable readRunnable = () -> {
                                    Reader reader = new Reader(client);
                                    reader.deployWorker(uuid);
                                };
                                executorService.submit(readRunnable);
                            }
                        }
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