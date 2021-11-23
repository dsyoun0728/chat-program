package server.worker;

import packet.ResponsePacket;
import parser.ParsedMsg;
import server.Client;
import server.Server;
import util.Constants;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SendTextWorker implements Worker{
    private Client client;
    private UUID uuid;

    public SendTextWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        ParsedMsg parsedMsg = this.client.getRequestParser().parseMessage(this.client.getRequestPacketList(this.uuid));
        String contentsStr = this.client.getUserNick() + "> ";
        contentsStr += new String(parsedMsg.getContents(), StandardCharsets.UTF_8);

        UUID uuidForOther = UUID.randomUUID();
        for (Client c : Server.getClientList()) {
            if (!c.equals(this.client)) {
                ResponsePacket responsePacket = new ResponsePacket(
                        uuidForOther,
                        (byte) Constants.RESPONSE_SUCCESS,
                        parsedMsg.getFunctionName(),
                        contentsStr.getBytes(StandardCharsets.UTF_8),
                        this.client.getUserNick().getBytes(StandardCharsets.UTF_8)
                );
                Server.getQueue().offer(Worker.createWriteRunnable(c, responsePacket.responsePacketList));
            }
        }
    }
}
