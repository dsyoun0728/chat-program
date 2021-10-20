import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    ExecutorService executorService;
    ServerSocketChannel serverSocketChannel;
    Selector selector;

    // 연결된 클라이언트를 관리할 컬렉션
    List<Client> connections = new Vector<Client>();

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
            System.out.println("IOException");
            e.printStackTrace();
            if (serverSocketChannel.isOpen()) { stopServer(); }
            return;
        }
        catch (Exception e) {
            System.out.println("Exception");
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
                    // 현재 순서의 처리할 이벤트를 임시 저장하고 Iterator에서 지워줌
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    // 연결 요청중인 클라이언트를 처리할 조건문 작성
                    if (selectionKey.isAcceptable()) {
                        accept(selectionKey);
                        // 읽기 이벤트(클라이언트 -> 서버)가 발생한 경우
                    } else if (selectionKey.isReadable()) {
                        selectionKey.interestOps(0);
                        selector.wakeup();
                        Client client = (Client) selectionKey.attachment();         // 클라이언트 객체 얻기
                        client.receive(selectionKey);
                        // 쓰기 이벤트(서버 -> 클라이언트)가 발생한 경우
                    } else if (selectionKey.isWritable()){
                        selectionKey.interestOps(0);
                        selector.wakeup();
                        Client client = (Client) selectionKey.attachment();
                        client.send(selectionKey);
                    }
                }
            } catch (IOException e) {
                System.out.println("IOException");
                e.printStackTrace();
                if (serverSocketChannel.isOpen()) { stopServer(); }
                break;
            } catch (Exception e) {
                System.out.println("Exception");
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
            System.out.println("IOException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }

    void accept(SelectionKey selectionKey) {
        try {
            // 연결 요청중인 이벤트이므로 해당 요청에 대한 소켓 채널을 생성해줌
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel(); // SelectionKey에 대한 채널 객체 얻기
            SocketChannel socketChannel = serverSocketChannel.accept();

            System.out.println("연결 수락: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]");

            // 연결된 클라이언트를 컬렉션에 추가
            Client client = new Client(socketChannel);
            connections.add(client);
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
            if (serverSocketChannel.isOpen()) { stopServer(); }
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
            if (serverSocketChannel.isOpen()) { stopServer(); }
        }

    }

    // 실제 Client가 아닌 Server입장에서의 Client
    class Client {
        SocketChannel socketChannel;                                                                // 여기서의 SocketChannel은 서버쪽의 것
        String sendData;                                                                            // 클라이언트로 보낼 데이터를 저장하는 필드

        Client(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;                                                     // 매개값으로 socketChannel 필드 초기화
            socketChannel.configureBlocking(false);                                              // 넌블로킹으로 설정
            SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);     // 읽기 작업 유형으로 Selector에 등록
            selectionKey.attach(this);                                                           // SelectionKey에 자기 자신을 첨부 객체로 저장
        }

        // 클라이언트 -> 서버로 메시지 보냈을 때(서버 - 읽기 이벤트 -> 클라이언트들에게 전송)
        void receive(SelectionKey selectionKey) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(100);

                        //상대방이 비정상 종료를 했을 경우 자동 IOException 발생
                        int byteCount = socketChannel.read(byteBuffer);                                     // socketChannel read() 데이터 받기

                        //상대방이 SocketChannel의 close() 메소드를 호출할 경우
                        if (byteCount == -1) {
                            throw new IOException();
                        }

                        System.out.println("[요청 처리: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]");

                        byteBuffer.flip();                                                                  // 데이터가 저장된 ByteBuffer의 flip() 메소드 호출해 위치 속성값 변경
                        Charset charset = Charset.forName("UTF-8");                                         // Charset 방식 UTF-8으로 설정
                        String data = charset.decode(byteBuffer).toString();                                // UTF-8로 디코딩한 문자열 얻기

                        // 모든 클라이언트에게 문자열을 전송하는 코드
                        for (Client client : connections) {
                            client.sendData = data;
                            SelectionKey key = client.socketChannel.keyFor(selector);                       // Client의 통신 채널로부터 SelectionKey 얻기
                            key.interestOps(SelectionKey.OP_WRITE);                                         // 채널의 작업 유형을 쓰기 작업 유형으로 변경
                        }
                        // 변경된 작업 유형을 감지하도록 하기 위해 Selector의 select() 블로킹 해제하고 다시 실행하도록 함
                        selector.wakeup();
                    } catch (Exception e) {
                        try {
                            connections.remove(this);                                                       // 예외 발생 시 connections에서 해당 Client 객체 제거
                            System.out.println("[클라이언트 통신 안됨: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]");
                            socketChannel.close();                                                          // SocketChannel 닫기
                        } catch (IOException e2) {
                        }
                    }
                }
            };
            executorService.submit(task);
        }


        void send(SelectionKey selectionKey) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        Charset charset = Charset.forName("UTF-8");
                        ByteBuffer byteBuffer = charset.encode(sendData);                                   // 보내고자하는 데이터 인코딩
                        socketChannel.write(byteBuffer);                                                    // ByteBuffer의 내용 클라이언트로 전송
                        selectionKey.interestOps(SelectionKey.OP_READ);                                     // 작업 유형을 읽기 작업 유형으로 변경
                        selector.wakeup();                                                                  // select() 블로킹 해제
                    } catch (Exception e) {
                        try {
                            System.out.println("[클라이언트 통신 안됨: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]");
                            connections.remove(this);                                                       // 예외가 발생한 Client 제거
                            socketChannel.close();                                                             // SocketChannel 닫기
                        } catch (IOException e2) {
                        }
                    }
                }
            };
            executorService.submit(task);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}
