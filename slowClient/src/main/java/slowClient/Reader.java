package slowClient;

import parser.ParsedMsg;
import parser.Parser;
import util.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class Reader {
    private slowClient slowClient;

    public Reader(slowClient slowClient) {
        this.slowClient = slowClient;
    }

    public void receive() {
        Runnable readRunnable = () -> {
            while (true) {
                try {
                    int byteCount = this.slowClient.getSocketChannel().read(slowClient.getReadByteBuffer());
                    slowClient.setByteCount(slowClient.getByteCount()+byteCount);

                    if (slowClient.getByteCount() == -1) {
                        throw new IOException();
                    }

                    if (slowClient.getByteCount() < Constants.PACKET_TOTAL_SIZE ){
                        continue;
                    }

                    slowClient.getReadByteBuffer().flip();
                    byte[] responsePacket = new byte[slowClient.getReadByteBuffer().remaining()];
                    slowClient.getReadByteBuffer().get(responsePacket);

                    slowClient.getReadByteBuffer().clear();
                    UUID uuid = Parser.getUUID(responsePacket);
                    if (!this.slowClient.getResponsePacketListMap().containsKey(uuid)) {
                        this.slowClient.initResponsePacketList(uuid, new ArrayList<>());
                    }
                    this.slowClient.getResponsePacketListMap().get(uuid).add(responsePacket);
                    String functionName = Parser.getFunctionName(responsePacket);

                    slowClient.setByteCount(0);

                    if (Parser.isLast(responsePacket)) {
                        ParsedMsg parsedMsg = this.slowClient.getResponseParser().parseMessage(this.slowClient.getResponsePacketList(uuid));
                        if (functionName.equals("DownloadFile")) {
                            String fileName = new String(parsedMsg.getOptionalInfo(), StandardCharsets.UTF_8);
                            String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s/_.]";
                            fileName = fileName.replaceAll(match, "");
                            Path path = Paths.get("../" + fileName +"_download");

                            System.out.println("로컬에 쓰기 작업 중");
                            Files.write(path, parsedMsg.getContents());
                            System.out.println("File Download 완료");
                        } else if (functionName.equals("Logout")) {
                            System.out.println("Logout 되었습니다");
                            this.slowClient.stopClient();
                            break;
                        } else {
                            String contentsStr = new String(parsedMsg.getContents(), StandardCharsets.UTF_8);
                            System.out.println(contentsStr);
                        }
                        this.slowClient.clearResponsePacketList(uuid);
                    }

                } catch (Exception e) {
                    System.out.println("서버 통신 안됨");
                    this.slowClient.stopClient();
                    break;
                }
            }
        };
        this.slowClient.getExecutorService().submit(readRunnable);
    }
}
