package client;

import packet.RequestPacket;
import parser.Parser;
import parser.ResponseParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    ExecutorService executorService;
    SocketChannel socketChannel;
    ArrayList<byte[]> packetByteArrayList = new ArrayList<byte[]>();
    Parser responseParser = new ResponseParser();

    void startClient() {
        try {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(5001));
            System.out.println("연결 완료");

        } catch (IOException e) {
            System.out.println("startClient try-catch block IOException\n\n\n" + e + "\n\n\n");
            if (socketChannel.isOpen()) { stopClient(); }
            return;
        } catch (Exception e) {
            System.out.println("startClient try-catch block Exception\n\n\n" + e + "\n\n\n");
            if (socketChannel.isOpen()) { stopClient(); }
            return;
        }
        receive();
    }

    void stopClient() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) { socketChannel.close(); }
        } catch (IOException e) {
            System.out.println("stopClient IOException\n\n\n" + e + "\n\n\n");
        } catch (Exception e) {
            System.out.println("stopClient Exception\n\n\n" + e + "\n\n\n");
        }
    }

    void receive() {
        Runnable readRunnable = () -> {
            while (true) {
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(120);

                    int readByteCount = socketChannel.read(byteBuffer);

                    if (readByteCount == -1) {
                        throw new IOException();
                    }

                    byteBuffer.flip();
                    byte[] responsePacketByteArray = byteBuffer.array();
                    packetByteArrayList.add(responsePacketByteArray);

                    if(responseParser.isLast(responsePacketByteArray)) {
                        String contentsStr = new String(responseParser.getContents(packetByteArrayList),StandardCharsets.UTF_8);
                        System.out.println(contentsStr);
                        packetByteArrayList.clear();
                    }
                } catch (Exception e) {
                    System.out.println("서버 통신 안됨");
                    stopClient();
                    break;
                }
            }
        };
        executorService.submit(readRunnable);
    }

    private void send(ArrayList<byte[]> byteArrayList) {
        Runnable writeRunnable = () -> {
            try {
                for (byte[] byteArray : byteArrayList) {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
                    socketChannel.write(byteBuffer);
                }
            } catch (IOException e) {
                System.out.println("client send IOException\n\n\n" + e + "\n\n\n");
                stopClient();
            } catch (Exception e) {
                System.out.println("client send Exception\n\n\n" + e + "\n\n\n");
                stopClient();
            }
        };
        executorService.submit(writeRunnable);
    }


    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();

        // ID 입력하기
        String userNick;
        Scanner sc = new Scanner(System.in);
        userNick = sc.nextLine();
        RequestPacket rp = new RequestPacket(
                "SendText",
                userNick.getBytes(StandardCharsets.UTF_8),
                "1".getBytes(StandardCharsets.UTF_8)
        );
        client.send(rp.requestPacketList);

        String contentsStr;
        while(true) {
            contentsStr = sc.nextLine();
            RequestPacket requestPacket = new RequestPacket(
                    "SendText",
                    contentsStr.getBytes(StandardCharsets.UTF_8),
                    "1".getBytes(StandardCharsets.UTF_8)
            );
            client.send(requestPacket.requestPacketList);
        }

//        // request packet 제작 예시
//        Scanner scanner = new Scanner(System.in);//
//        String contentsStr = scanner.nextLine();
//        String userNick = scanner.nextLine();
//        RequestPacket requestPacket = new RequestPacket(
//                "SendFile",
//                true,
//                contentsStr.getBytes(StandardCharsets.UTF_8),
//                userNick.getBytes(StandardCharsets.UTF_8)
//        );
    }


}
