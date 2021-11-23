package server.worker;

import packet.ResponsePacket;
import parser.ParsedMsg;
import server.Client;
import server.Server;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

public class DownloadFileWorker implements Worker {
    private Client client;
    private UUID uuid;

    public DownloadFileWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        ParsedMsg parsedMsg = this.client.getRequestParser().parseMessage(this.client.getRequestPacketList(this.uuid));
        ResponsePacket responsePacket;
        String filePath = "../chat-program-data/";
        String fileName = new String(parsedMsg.getContents(), StandardCharsets.UTF_8);
        if (Server.getFileList().isEmpty()) {
            responsePacket = new ResponsePacket(
                    this.uuid,
                    (byte) Constants.RESPONSE_SUCCESS,
                    parsedMsg.getFunctionName(),
                    "file이 없습니다".getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
        } else {
            byte[] fileContent = new byte[0];
            try {
                filePath += fileName;
                File file = new File(filePath);
                fileContent = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                System.out.println("DownloadFileWorker IOException\n\n\n");
                e.printStackTrace();
                Worker.handleClientOut(this.client, this.uuid);
            }
            responsePacket = new ResponsePacket(
                    this.uuid,
                    (byte) Constants.RESPONSE_SUCCESS,
                    parsedMsg.getFunctionName(),
                    fileContent,
                    fileName.getBytes(StandardCharsets.UTF_8)
            );
        }

        this.client.setResponsePacketList(this.uuid, responsePacket.responsePacketList);
        this.client.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        Server.getCallback().completed(null, null);
    }
}
