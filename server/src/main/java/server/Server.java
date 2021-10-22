import packet.ResponsePacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    ExecutorService executorService;
    Selector selector;
    ServerSocketChannel serverSocketChannel;
    List<Client> connections = new Vector<Client>();
    Charset charset = Charset.forName("UTF-8");

    void startServer() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress("192.168.14.59", 5001));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("startServer");
        } catch (IOException e){
            System.out.println("startServer try-catch block IOException\n\n\n" );
            e.printStackTrace();
            if (serverSocketChannel.isOpen()) { stopServer(); }
            return;
        } catch (Exception e){
            System.out.println("startServer try-catch block Exception\n\n\n");
            e.printStackTrace();
            if (serverSocketChannel.isOpen()) { stopServer(); }
            return;
        }

        while(true) {
            try {
                int keyCount = selector.select();
                if (keyCount == 0) {
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()) {
                        accept(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        Client client = (Client)selectionKey.attachment();
                        selectionKey.interestOps(0);
                        client.receive(selectionKey);
                    }
                    /*
                    selector에서 OP_WRITE는 사용하지 않을 예정
                    else if (selectionKey.isWritable()) {
                        Client client = (Client)selectionKey.attachment();
                        client.send(selectionKey);
                    }
                    */
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

    void stopServer() {
        try{
            Iterator<Client> iterator = connections.iterator();
            while(iterator.hasNext()) {
                Client client = iterator.next();
                client.socketChannel.close();
                iterator.remove();
            }
            if (serverSocketChannel!=null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            if (selector!=null && selector.isOpen()) {
                selector.close();
            }
            if (executorService!=null && !executorService.isShutdown()) {
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

            String acceptInfo = "client-" + (connections.size()+1) + "connected : " + socketChannel.getRemoteAddress();
            System.out.println(acceptInfo);

            Client client = new Client(socketChannel);
            connections.add(client);

        } catch (IOException e) {
            System.out.println("accept IOException\n\n\n");
            e.printStackTrace();
            if (serverSocketChannel.isOpen()) { stopServer(); }
        } catch (Exception e) {
            System.out.println("accept Exception\n\n\n");
            e.printStackTrace();
            if (serverSocketChannel.isOpen()) { stopServer(); }
        }
    }

    class Client {
        SocketChannel socketChannel;
        String sendData;

        Client(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
            selectionKey.attach(this);
        }

        void receive(SelectionKey selectionKey) {
            Runnable readRunnable = () -> {
                try {
                    ByteBuffer readByteBuffer = ByteBuffer.allocate(100);
                    int byteCount = socketChannel.read(readByteBuffer);
                    if (byteCount == -1) {
                        throw new IOException("클라이언트 연결 정상적으로 끊김" + socketChannel.getRemoteAddress());
                    }
                    readByteBuffer.flip();
//                    // wirteByteBuffer는 readByteBuffer를 가공해야함
//                    ByteBuffer writeByteBuffer = readByteBuffer;

                    // data 확인용
                    String data = charset.decode(readByteBuffer).toString();
                    String message = "[요청 처리: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]";
                    System.out.println(message + " " + data);

//                    // 채팅방에 broadcast
//                    for (Client client : connections) {
//                        client.send(writeByteBuffer);
//                    }

                    selectionKey.interestOps(SelectionKey.OP_READ);
                    selector.wakeup();
                } catch (IOException e) {
                    System.out.println("server receive IOException\n\n\n");
                    e.printStackTrace();
                    try {
                        connections.remove(this);
                        String message = "[클라이언트 통신 안됨: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]";
                        System.out.println(message);
                        socketChannel.close();                                                          // SocketChannel 닫기
                    } catch (IOException e2) { System.out.println("receive socketChannel close IOException\n\n\n" + e2 + "\n\n\n"); }
                } catch (Exception e) {
                    System.out.println("server receive Exception\n\n\n");
                    e.printStackTrace();
                }
            };
            executorService.submit(readRunnable);
        }

        void send(ByteBuffer writeByteBuffer) {
            // send에는 packet을 이미 capsule화해서 매개값으로 넣을 것
            Runnable writeRunnable = () -> {
                try {
                    socketChannel.write(writeByteBuffer);
                } catch (IOException e) {
                    System.out.println("server send IOException\n\n\n");
                    e.printStackTrace();
                    try {
                        connections.remove(this);
                        String message = "[클라이언트 통신 안됨: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]";
                        System.out.println(message);
                        socketChannel.close();
                    } catch (IOException e2) { System.out.println("receive socketChannel close IOException\n\n\n" + e2 + "\n\n\n"); }
                } catch (Exception e) {
                    System.out.println("server send Exception\n\n\n");
                    e.printStackTrace();
                }
            };
            executorService.submit(writeRunnable);
        }


    }
    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
//        // response packet 제작 예시
//        ResponsePacket responsePacket = new ResponsePacket(
//                (byte) 20,
//                (byte) 16,
//                true,
//                "200".getBytes(StandardCharsets.UTF_8),
//                "nonono".getBytes(StandardCharsets.UTF_8)
//        );
    }
}
