package server;

import parser.Parser;
import server.worker.Worker;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static ExecutorService executorService;
    private ServerSocketChannel serverSocketChannel;
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

    public static List<Client> getClientList() { return clientList; }
    public static List<String> getFileList() { return fileList; }
    public static Queue<Runnable> getQueue() { return queue; }
    public static Selector getSelector() { return selector; }
    public static ExecutorService getExecutorService() { return executorService; }
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
                if (queue.peek()!=null) {
                    queue.poll().run();
                    selector.selectNow();
                } else {
                    selector.select();
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    if (selectionKey.isAcceptable()) {
                        accept(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        selectionKey.interestOps(0);
                        selector.wakeup();

                        Client client = (Client) selectionKey.attachment();
                        Runnable readRunnable = () -> {
                            try {
                                int byteCount = client.getSocketChannel().read(client.getReadByteBuffer());
                                client.setByteCount(client.getByteCount() + byteCount);

                                //상대방이 SocketChannel의 close() 메소드를 호출할 경우
                                if (client.getByteCount() == -1) {
                                    System.out.println("클라이언트 연결 정상적으로 끊김" + client.getSocketChannel().getRemoteAddress());
                                    Server.setClientList(false, client);
                                    return;
                                }

                                if (0 < client.getByteCount() && client.getByteCount() < Constants.PACKET_TOTAL_SIZE) {
                                    selectionKey.interestOps(SelectionKey.OP_READ);
                                    selector.wakeup();
                                    return;
                                }

                                // 정상 동작 시작
                                client.getReadByteBuffer().flip();
                                byte[] requestPacket = new byte[client.getReadByteBuffer().remaining()];
                                client.getReadByteBuffer().get(requestPacket);

                                client.getReadByteBuffer().clear();
                                UUID uuid = Parser.getUUID(requestPacket);
                                if (!client.getRequestPacketListMap().containsKey(uuid)) {
                                    client.getRequestPacketListMap().put(uuid, new ArrayList<>());
                                }
                                client.getRequestPacketList(uuid).add(requestPacket);
                                client.setByteCount(0);

                                if (Parser.isLast(requestPacket)) {
                                    Reader reader = new Reader(client);
                                    reader.deployWorker(uuid);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        };
                        readRunnable.run();
                        selectionKey.interestOps(SelectionKey.OP_READ);
                        selector.wakeup();
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