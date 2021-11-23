package client;

import parser.ParsedMsg;
import parser.Parser;
import util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class Reader {
    private Client client;

    public Reader(Client client) {
        this.client = client;
    }

    public void receive() {
        Runnable readRunnable = () -> {
            while (true) {
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.PACKET_TOTAL_SIZE);
                    int byteCount = this.client.getSocketChannel().read(byteBuffer);

                    if (byteCount == -1) {
                        throw new IOException();
                    }

                    while ( 0 < byteCount && byteCount < Constants.PACKET_TOTAL_SIZE ){
                        byteCount += this.client.getSocketChannel().read(byteBuffer);
                    }

                    byteBuffer.flip();
                    byte[] responsePacket = byteBuffer.array();
                    UUID uuid = Parser.getUUID(responsePacket);
                    if (!this.client.getResponsePacketListMap().containsKey(uuid)) {
                        this.client.initResponsePacketList(uuid, new ArrayList<byte[]>());
                    }
                    this.client.getResponsePacketListMap().get(uuid).add(responsePacket);
                    String functionName = Parser.getFunctionName(responsePacket);

                    if (Parser.isLast(responsePacket)) {
                        ParsedMsg parsedMsg = this.client.getResponseParser().parseMessage(this.client.getResponsePacketList(uuid));
                        if (functionName.equals("DownloadFile")) {
                            System.out.println("DownloadFile" + parsedMsg.getUuid().toString());

                            String fileName = new String(parsedMsg.getOptionalInfo(), StandardCharsets.UTF_8);
                            System.out.println("fileName: " + fileName);
                            String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s/_.]";
                            fileName = fileName.replaceAll(match, "");
                            Path path = Paths.get("../" + fileName +"_download");

                            System.out.println("로컬에 쓰기 작업 중");
                            Files.write(path, parsedMsg.getContents());
                            System.out.println("File Download 완료");
                        } else if (functionName.equals("Logout")) {
                            System.out.println("Logout 되었습니다");
                            this.client.stopClient();
                            break;
                        } else {
                            String contentsStr = new String(parsedMsg.getContents(), StandardCharsets.UTF_8);
                            System.out.println(contentsStr);
                        }
                        this.client.clearResponsePacketList(uuid);
                    }
                } catch (Exception e) {
                    System.out.println("서버 통신 안됨");
                    this.client.stopClient();
                    break;
                }
            }
        };
        this.client.getExecutorService().submit(readRunnable);
    }
}
