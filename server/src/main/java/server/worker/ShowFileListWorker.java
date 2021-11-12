package server.worker;

import packet.ResponsePacket;
import server.Client;
import server.Server;

import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;

public class ShowFileListWorker implements Worker {
    private Client client;

    public ShowFileListWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        ResponsePacket responsePacket;
        if (Server.getFileList().isEmpty() ){
            responsePacket = new ResponsePacket(
                    (byte) 20,
                    (byte) 16,
                    "file이 없습니다".getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
        } else {
            String fileName = "";
            for (String s : Server.getFileList()) {
                fileName += s + "\n";
            }
            responsePacket = new ResponsePacket(
                    (byte) 20,
                    (byte) 16,
                    fileName.getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
        }

        this.client.setResponsePacketList(responsePacket.responsePacketList);
        this.client.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        Server.getCallback().completed(null, null);
    }
}
