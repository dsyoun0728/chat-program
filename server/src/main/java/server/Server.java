package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ExecutorService executorService;
    private ServerSocketChannel serverSocketChannel;
    private static Selector selector;
    private static List<Client> clientList = new CopyOnWriteArrayList<>();
    private static List<String> fileList = new CopyOnWriteArrayList<>();
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
    public static CompletionHandler<Void, Void> getCallback() { return callback; }
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
                int keyCount = selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    if (selectionKey.isAcceptable()) {
                        accept(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        selectionKey.interestOps(0);
                        Client client = (Client) selectionKey.attachment();
                        executorService.submit(client.makeWholePacket());
                    } else if (selectionKey.isWritable()){
                        selectionKey.interestOps(0);
                        Writer writer = new Writer((Client) selectionKey.attachment());
                        executorService.submit(writer.writeToChannel());
                    }
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