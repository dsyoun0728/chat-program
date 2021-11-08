package client;

import packet.RequestPacket;
import parser.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    ExecutorService executorService;
    SocketChannel socketChannel;
    ArrayList<byte[]> packetByteArrayList = new ArrayList<byte[]>();
    Parser responseParser = new ResponseParser();

    void startClient(Client client, String userNick) {
        try {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(5001));
            System.out.println("서버 연결 완료");
            RequestPacket loginPacket = new RequestPacket(
                    "Login",
                    userNick.getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
            client.send(loginPacket.requestPacketList);
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


    public static void main(String[] args) throws IOException {
        Client client = new Client();
        System.out.print("userNick을 입력하세요 > ");
        Scanner sc = new Scanner(System.in);
        String userNick;
        userNick = sc.nextLine();
        client.startClient(client, userNick);

        String contentsStr;
        while(true) {
            contentsStr = sc.nextLine();

            if ( contentsStr.equals("SendFile") ) {
                System.out.print("업로드할 파일 경로를 입력하세요 > ");
                String filePath;
                filePath = sc.nextLine();

                File file = new File(filePath);
                byte[] fileContent = Files.readAllBytes(file.toPath());

                RequestPacket requestPacket = new RequestPacket(
                        "SendFile",
                        fileContent,
                        filePath.getBytes(StandardCharsets.UTF_8)
                );
                client.send(requestPacket.requestPacketList);
            } else if ( contentsStr.equals("ShowFileList") ) {
                RequestPacket requestPacket = new RequestPacket(
                        "ShowFileList",
                        "Temp".getBytes(StandardCharsets.UTF_8),
                        "".getBytes(StandardCharsets.UTF_8)
                );
                client.send(requestPacket.requestPacketList);
            } else if ( contentsStr.equals("DownloadFile") ) {
                System.out.print("다운로드할 파일 이름을 입력하세요 > ");
                String fileName;
                fileName = sc.nextLine();

                RequestPacket requestPacket = new RequestPacket(
                        "DownloadFile",
                        fileName.getBytes(StandardCharsets.UTF_8),
                        "".getBytes(StandardCharsets.UTF_8)
                );
                client.send(requestPacket.requestPacketList);
            } else {
                RequestPacket requestPacket = new RequestPacket(
                        "SendText",
                        contentsStr.getBytes(StandardCharsets.UTF_8),
                        "".getBytes(StandardCharsets.UTF_8)
                );
                client.send(requestPacket.requestPacketList);
            }
        }
    }


}
