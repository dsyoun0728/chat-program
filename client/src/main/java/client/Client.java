package client;

import packet.RequestPacket;
import packet.ResponsePacket;
import parser.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    ExecutorService executorService;
    SocketChannel socketChannel;
    ArrayList<byte[]> packetByteArrayList = new ArrayList<byte[]>();
    Parser responseParser = new ResponseParser();
    static String fileName;

    void startClient(Client client, String ipAndport, String userNick) {
        try {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            String[] ipAndportArray = ipAndport.split(":");
            socketChannel.connect(new InetSocketAddress( ipAndportArray[0], Integer.parseInt(ipAndportArray[1])));
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

                    int byteCount = socketChannel.read(byteBuffer);
                    //System.out.println(byteCount);

                    if (byteCount == -1) {
                        throw new IOException();
                    }

                    while ( 0 < byteCount && byteCount < 120 ){
                        byteCount += socketChannel.read(byteBuffer);
                    }
                    //System.out.println(byteCount);

                    //System.out.println("[요청 처리: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]");

                    byteBuffer.flip();
                    byte[] responsePacketByteArray = byteBuffer.array();
                    packetByteArrayList.add(responsePacketByteArray);
                    String functionName = responseParser.getFunctionName(packetByteArrayList);
                    if (responseParser.isLast(responsePacketByteArray)) {
                        if (functionName.equals("DownloadFile")) {
                            byte[] fileContents = responseParser.getContents(packetByteArrayList);
                            packetByteArrayList.clear();

                            Path path = Paths.get("../" + Client.fileName +"_download");
                            System.out.println("로컬에 쓰기 작업 중");
                            Files.write(path, fileContents);
                            System.out.println("File Download 완료");
                        } else {
                            String contentsStr = new String(responseParser.getContents(packetByteArrayList), StandardCharsets.UTF_8);
                            System.out.println(contentsStr);
                            packetByteArrayList.clear();
                        }
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
                String fn = responseParser.getFunctionName(byteArrayList);
                if (fn.equals("SendFile")) {
                    System.out.println("파일을 서버에 전송 중입니다....");
                }
                for (byte[] byteArray : byteArrayList) {
                    int sendCount = 0;
                    ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
                    while (sendCount < 120) {
                        sendCount += socketChannel.write(byteBuffer);
                    }
                }
                if (fn.equals("SendFile")) {
                    System.out.println("서버가 처리 중입니다....");
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
        Scanner sc = new Scanner(System.in);

        System.out.println("안녕하세요. 로그인 후 다른 기능들을 이용 가능합니다.");
        System.out.printf("접속 서버 IP 주소와 Port를 입력하세요(ex. 192.168.14.51:5001 ) > ");
        String ipAndport = sc.nextLine();

        System.out.print("userNick을 입력하세요 > ");
        String userNick;
        userNick = sc.nextLine();
        client.startClient(client, ipAndport, userNick);

        String contentsStr;
        while(true) {
            contentsStr = sc.nextLine();

            if ( contentsStr.equals("SendFile") ) {
                System.out.print("업로드 할 파일 경로를 입력하세요 > ");
                String filePath;
                filePath = sc.nextLine();

                File file = new File(filePath);
                byte[] fileContent = Files.readAllBytes(file.toPath());

                String[] filePathArray = filePath.split("/");
                filePath = filePathArray[filePathArray.length-1];

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
                System.out.print("다운로드 할 파일 이름을 입력하세요 > ");
                Client.fileName = sc.nextLine();

                RequestPacket requestPacket = new RequestPacket(
                        "DownloadFile",
                        Client.fileName.getBytes(StandardCharsets.UTF_8),
                        "".getBytes(StandardCharsets.UTF_8)
                );
                client.send(requestPacket.requestPacketList);
            } else {
                RequestPacket requestPacket = new RequestPacket(
                        "SendText",
                        contentsStr.getBytes(StandardCharsets.UTF_8),
                        "1".getBytes(StandardCharsets.UTF_8)
                );
                client.send(requestPacket.requestPacketList);
            }
        }
    }


}
