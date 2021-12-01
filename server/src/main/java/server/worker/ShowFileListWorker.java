package server.worker;

import packet.ResponsePacket;
import parser.ParsedMsg;
import server.Client;
import server.Server;
import util.Constants;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ShowFileListWorker implements Worker {
    private Client client;
    private UUID uuid;

    public ShowFileListWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        ParsedMsg parsedMsg = Server.getRequestParser().parseMessage(this.client.getRequestPacketList(this.uuid));
        ResponsePacket responsePacket;
        if (Server.getFileList().isEmpty() ){
            responsePacket = new ResponsePacket(
                    this.uuid,
                    (byte) Constants.RESPONSE_SUCCESS,
                    parsedMsg.getFunctionName(),
                    "file이 없습니다".getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
        } else {
            String fileName = "";
            for (String s : Server.getFileList()) {
                fileName += s + "\n";
            }
            responsePacket = new ResponsePacket(
                    this.uuid,
                    (byte) Constants.RESPONSE_SUCCESS,
                    parsedMsg.getFunctionName(),
                    fileName.getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
        }

        Worker.createWriteQueue(this.client, responsePacket.responsePacketList);
        this.client.clearRequestPacketList(this.uuid);
    }
}
