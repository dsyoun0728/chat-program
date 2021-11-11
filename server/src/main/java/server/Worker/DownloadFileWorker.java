package server.Worker;

import packet.ResponsePacket;
import server.Client;
import server.Server;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class DownloadFileWorker implements Worker {
    private Client client;

    public DownloadFileWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        ResponsePacket responsePacket;
        String filePath = "../../../../../../../chat-program-data/";
        if (Server.getFileList().isEmpty()) {
            responsePacket = new ResponsePacket(
                    (byte) 20,
                    (byte) 16,
                    "file이 없습니다".getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
        } else {
            byte[] fileContent = new byte[0];
            try {
                filePath += new String(this.client.getRequestParser().getContents(this.client.getRequestPacketList()), StandardCharsets.UTF_8);
                File file = new File(filePath);
                fileContent = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                System.out.println("DownloadFileWorker IOException\n\n\n");
                e.printStackTrace();
                Worker.handleClientOut(this.client);
            }
            responsePacket = new ResponsePacket(
                    (byte) 20,
                    (byte) 17,
                    fileContent,
                    filePath.getBytes(StandardCharsets.UTF_8)
            );
        }

        this.client.setResponsePacketList(responsePacket.responsePacketList);
        this.client.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        Server.getCallback().completed(null, null);
    }
}
