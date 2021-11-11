package server.Worker;

import packet.ResponsePacket;
import server.Client;
import server.Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SendFileWorker implements Worker {
    private Client client;

    public SendFileWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        try {
            byte[] fileBytes = this.client.getRequestParser().getContents(this.client.getRequestPacketList());
            byte[] optionalInfo = this.client.getRequestParser().getOptionalInfo(this.client.getRequestPacketList());
            String filePath = new String(optionalInfo, StandardCharsets.UTF_8);
            String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s/_.]";
            filePath = filePath.replaceAll(match, "");
            Server.setFileList(true, filePath);

            Path path = Paths.get("../../../../../../../chat-program-data/" + filePath);
            Files.write(path, fileBytes);
        } catch (IOException e) {
            System.out.println("SendFileWorker IOException\n\n\n");
            e.printStackTrace();
            Worker.handleClientOut(this.client);
        }

        this.client.clearRequestPacketList();

        ResponsePacket responsePacket = new ResponsePacket(
                (byte) 20,
                (byte) 6,
                "Server> 파일 전송이 완료되었습니다".getBytes(StandardCharsets.UTF_8),
                "".getBytes(StandardCharsets.UTF_8)
        );
        this.client.setResponsePacketList(responsePacket.responsePacketList);
        this.client.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        Server.getCallback().completed(null, null);
    }
}
