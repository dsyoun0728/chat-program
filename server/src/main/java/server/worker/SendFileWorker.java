package server.worker;

import packet.ResponsePacket;
import parser.ParsedMsg;
import server.Client;
import server.Server;
import util.Constants;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class SendFileWorker implements Worker {
    private Client client;
    private UUID uuid;

    public SendFileWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        ParsedMsg parsedMsg = this.client.getRequestParser().parseMessage(this.client.getRequestPacketList(this.uuid));
        Runnable localWriteRunnable = () -> {
            try {
                byte[] fileBytes = parsedMsg.getContents();
                byte[] optionalInfo = parsedMsg.getOptionalInfo();
                String fileName = new String(optionalInfo, StandardCharsets.UTF_8);
                String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s/_.]";
                fileName = fileName.replaceAll(match, "");
                Server.setFileList(true, fileName);
                Path path = Paths.get("../chat-program-data/" + fileName);
                System.out.println("Server 로컬에 쓰기 작업 중");
                Files.write(path, fileBytes);
                System.out.println("완료");
            } catch (IOException e) {
                System.out.println("SendFileWorker IOException\n\n\n");
                e.printStackTrace();
                Worker.handleClientOut(this.client, this.uuid);
            }
        };
        Server.getExecutorService().submit(localWriteRunnable);

        ResponsePacket responsePacket = new ResponsePacket(
                this.uuid,
                (byte) Constants.RESPONSE_SUCCESS,
                parsedMsg.getFunctionName(),
                "Server> 파일 수신이 완료되었습니다".getBytes(StandardCharsets.UTF_8),
                "".getBytes(StandardCharsets.UTF_8)
        );
        Worker.createWriteRunnable(this.client, responsePacket.responsePacketList);
        this.client.clearRequestPacketList(this.uuid);
    }
}
