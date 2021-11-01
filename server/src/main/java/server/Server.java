package server;

import parser.*;
import packet.ResponsePacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    ExecutorService executorService;
    ServerSocketChannel serverSocketChannel;
    Selector selector;

    // 연결된 클라이언트를 관리할 컬렉션
    List<Client> connections = new CopyOnWriteArrayList<Client>();

    void startServer() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            serverSocketChannel = ServerSocketChannel.open();
            // 서비스 포트 설정 및 논블로킹 모드로 설정
            serverSocketChannel.bind(new InetSocketAddress(5001));
            serverSocketChannel.configureBlocking(false);

            // Selector 생성 및 채널 등록
            selector = Selector.open();
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
                // select() - 이벤트 발생할 때까지 스레드 블로킹
                int keyCount = selector.select();

                // 발생한 이벤트를 모두 Iterator에 담아줌
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                // 발생한 이벤트들을 담은 Iterator의 이벤트를 하나씩 순서대로 처리함
                while (iterator.hasNext()) {
                    // 현재 순서의 처리할 이벤트를 SelectionKey에 임시 저장하고 Iterator에서 지워줌
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    // 연결 요청중인 클라이언트를 처리할 조건문 작성
                    if (selectionKey.isAcceptable()) {
                        accept(selectionKey);
                        // 읽기 이벤트(클라이언트 -> 서버)가 발생한 경우
                    } else if (selectionKey.isReadable()) {
                        selectionKey.interestOps(0);
                        Client client = (Client) selectionKey.attachment();         // 현재 클라이언트 객체 얻기
                        client.receive(selectionKey);
                        // 쓰기 이벤트(서버 -> 클라이언트)가 발생한 경우
                    } else if (selectionKey.isWritable()){
                        selectionKey.interestOps(0);
                        Client client = (Client) selectionKey.attachment();
                        client.send(selectionKey);
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
            Iterator<Client> iterator = connections.iterator();
            while (iterator.hasNext()) {
                Client client = iterator.next();
                client.socketChannel.close();                                               // 연결된 SocketChannel 닫기
                iterator.remove();
            }
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();                                                // ServerSocketChannel 닫기
            }
            if (selector != null && selector.isOpen()) {
                selector.close();                                                           // Selector 닫기
            }
            if (executorService != null && !executorService.isShutdown()){
                executorService.shutdown();                                                 // ExecutorService 닫기
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
            // 연결 요청중인 이벤트이므로 해당 요청에 대한 소켓 채널을 생성해줌
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel(); // SelectionKey에 대한 채널 객체 얻기
            SocketChannel socketChannel = serverSocketChannel.accept();

            String acceptInfo = "client-" + (connections.size()+1) + " connected : " + socketChannel.getRemoteAddress();
            System.out.println(acceptInfo);

            // 연결된 클라이언트를 컬렉션에 추가
            Client client = new Client(socketChannel);
            connections.add(client);

            // ID를 입력받기 위한 출력을 해당 클라이언트에 해줌
            ResponsePacket responsePacket = new ResponsePacket(
                    (byte) 20,
                    (byte) 4,
                    "Server> ID를 입력해주세요 : ".getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
            for(byte[] byteArray : responsePacket.responsePacketList) {
                socketChannel.write(ByteBuffer.wrap(byteArray));
            }
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
        SocketChannel socketChannel;                                                                // 여기서의 SocketChannel은 서버쪽의 것
        String userNick;
        boolean userNickRegist = false;                                                             // ID 등록 여부
        byte[] responsePacketByteArray;

        Client(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;                                                     // 매개값으로 socketChannel 필드 초기화
            socketChannel.configureBlocking(false);                                                 // 넌블로킹으로 설정
            SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);     // 읽기 작업 유형으로 Selector에 등록
            selectionKey.attach(this);                                                           // SelectionKey에 자기 자신을 첨부 객체로 저장
        }

        // 클라이언트 -> 서버로 메시지 보냈을 때(서버 - 읽기 이벤트 -> 클라이언트들에게 전송)
        void receive(SelectionKey selectionKey) {
            Runnable readRunnable = () -> {
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(200);

                    //상대방이 비정상 종료를 했을 경우 자동 IOException 발생
                    int byteCount = socketChannel.read(byteBuffer);                                     // socketChannel read() 데이터 받기

                    //상대방이 SocketChannel의 close() 메소드를 호출할 경우
                    if (byteCount == -1) {
                        throw new IOException("클라이언트 연결 정상적으로 끊김" + socketChannel.getRemoteAddress());
                    }

                    System.out.println("[요청 처리: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]");

                    // 현재 ID가 없으면 ID등록
                    Client client = (Client) selectionKey.attachment();                                 // 현재 클라이언트 객체 얻기
                    if(!client.userNickRegist){
                        client.userNickRegist = true;
                        byteBuffer.flip();

                        // ID 뽑기
                        byte[] requestPacketByteArray = byteBuffer.array();
                        RequestParser requestParser = new RequestParser(requestPacketByteArray);
                        byte[] originalContents = requestParser.contents;
                        byte[] id = new byte[requestParser.contentsLength];
                        System.arraycopy(originalContents,0,id,0,id.length);
                        client.userNick = new String(id,StandardCharsets.UTF_8);
                        byteBuffer.clear();

                        // 서버에 출력
                        System.out.println(client.userNick +"님이 입장하셨습니다");

                        // 다른 Client들에게 출력
                        for (Client c : connections) {
                            if (!c.equals(client)) {
                                ResponsePacket responsePacket = new ResponsePacket(
                                        (byte) 20,
                                        (byte) 4,
                                        ("Server> "+client.userNick +"님이 입장하셨습니다").getBytes(StandardCharsets.UTF_8),
                                        "".getBytes(StandardCharsets.UTF_8)
                                );
                                for(byte[] byteArray : responsePacket.responsePacketList) {
                                    c.responsePacketByteArray = byteArray;
                                }
//                                c.responsePacketByteArray = responsePacket.responsePacketByteArray;
                                SelectionKey key = c.socketChannel.keyFor(selector);                            // Client의 통신 채널로부터 SelectionKey 얻기
                                key.interestOps(SelectionKey.OP_WRITE);                                         // Key의 작업 유형 변경
                            } else {
                                SelectionKey key = c.socketChannel.keyFor(selector);                            // Client의 통신 채널로부터 SelectionKey 얻기
                                key.interestOps(SelectionKey.OP_READ);                                          // Key의 작업 유형 변경
                            }
                        }
                        selector.wakeup();
                        return;
                    }

                    // broadcast 준비
                    byteBuffer.flip();
                    byte[] requestPacketByteArray = byteBuffer.array();
                    RequestParser requestParser = new RequestParser(requestPacketByteArray);
                    byte[] originalMessage = requestParser.contents;
                    byte[] message = new byte[requestParser.contentsLength];
                    System.arraycopy(originalMessage,0,message,0,message.length);

                    String msg = new String(message,StandardCharsets.UTF_8);
                    String userNickNotice = client.userNick+"> ";

                    // 자신을 제외한 모든 클라이언트에게 문자열을 전송하는 코드
                    for (Client c : connections) {
                        if (!c.equals(client)) {
                            ResponsePacket responsePacket = new ResponsePacket(
                                    (byte) 20,
                                    (byte) 4,
                                    true,
                                    (userNickNotice+msg).getBytes(StandardCharsets.UTF_8),
                                    "".getBytes(StandardCharsets.UTF_8)
                            );
                            c.responsePacketByteArray = responsePacket.responsePacketByteArray;
                            SelectionKey key = c.socketChannel.keyFor(selector);
                            key.interestOps(SelectionKey.OP_WRITE);
                        } else {
                            SelectionKey key = c.socketChannel.keyFor(selector);                            // Client의 통신 채널로부터 SelectionKey 얻기
                            key.interestOps(SelectionKey.OP_READ);                                          // Key의 작업 유형 변경
                        }
                    }
                    // 변경된 작업 유형을 감지하도록 하기 위해 Selector의 select() 블로킹 해제하고 다시 실행하도록 함
                    selector.wakeup();
                } catch (IOException e) {
                    System.out.println("server receive IOException\n\n\n");
                    e.printStackTrace();
                    try {
                        System.out.println("[클라이언트 통신 안됨: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]");
                        for(Client c : connections){
                            if(!c.equals(Client.this)) {
                                ResponsePacket responsePacket = new ResponsePacket(
                                        (byte) 20,
                                        (byte) 4,
                                        true,
                                        (Client.this.userNick +"님의 연결이 종료되었습니다").getBytes(StandardCharsets.UTF_8),
                                        "".getBytes(StandardCharsets.UTF_8)
                                );
                                c.responsePacketByteArray = responsePacket.responsePacketByteArray;
                                SelectionKey key = c.socketChannel.keyFor(selector);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }else {
                                SelectionKey key = c.socketChannel.keyFor(selector);                            // Client의 통신 채널로부터 SelectionKey 얻기
                                key.interestOps(SelectionKey.OP_READ);                                          // Key의 작업 유형 변경
                            }
                        }
                        connections.remove(Client.this);                                                       // 예외 발생 시 connections에서 해당 Client 객체 제거
                        selector.wakeup();

                        socketChannel.close();                                                          // SocketChannel 닫기
                    } catch (IOException e2) {
                        System.out.println("receive socketChannel close IOException\n\n\n" + e2 + "\n\n\n");
                        e2.printStackTrace();
                    }
                } catch (Exception e) {
                    System.out.println("server receive Exception\n\n\n");
                    e.printStackTrace();
                }
            };
            executorService.submit(readRunnable);
        }


        void send(SelectionKey selectionKey) {
            Runnable writeRunnable = () -> {
                try {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(responsePacketByteArray);
                    socketChannel.write(byteBuffer);

                    selectionKey.interestOps(SelectionKey.OP_READ);                                     // 작업 유형을 읽기 작업 유형으로 변경
                    selector.wakeup();                                                                  // select() 블로킹 해제
                } catch (IOException e) {
                    System.out.println("server send IOException\n\n\n");
                    e.printStackTrace();
                    try {
                        System.out.println("[클라이언트 통신 안됨: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]");
                        connections.remove(this);                                                       // 예외가 발생한 Client 제거
                        socketChannel.close();                                                             // SocketChannel 닫기
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