package server.worker;

import packet.ResponsePacket;
import parser.ParsedMsg;
import server.Client;
import server.Server;
import util.Constants;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LoginWorker implements Worker{
    private Client client;
    private UUID uuid;

    public LoginWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        ParsedMsg parsedMsg = Server.getRequestParser().parseMessage(this.client.getRequestPacketList(this.uuid));
        this.client.setUserNick(new String(parsedMsg.getContents(), StandardCharsets.UTF_8));

        // Server에 출력
        System.out.println(this.client.getUserNick() + "\t입장");

        for (Client c : Server.getClientList()) {
            if (!c.equals(this.client)) {
                ResponsePacket responsePacket = new ResponsePacket(
                        this.uuid,
                        (byte) Constants.RESPONSE_SUCCESS,
                        parsedMsg.getFunctionName(),
                        ("Server > " + this.client.getUserNick() + " 님이 입장하였습니다.").getBytes(StandardCharsets.UTF_8),
                        "1".getBytes(StandardCharsets.UTF_8)
                );
                Worker.createWriteQueue(c, responsePacket.responsePacketList);
            } else {
                ResponsePacket responsePacket = new ResponsePacket(
                        this.uuid,
                        (byte) Constants.RESPONSE_SUCCESS,
                        parsedMsg.getFunctionName(),
                        ("Server > " + this.client.getUserNick() + " (으)로 로그인 완료").getBytes(StandardCharsets.UTF_8),
                        "1".getBytes(StandardCharsets.UTF_8)
                );
                Worker.createWriteQueue(this.client, responsePacket.responsePacketList);
                this.client.clearRequestPacketList(this.uuid);
            }
        }
    }
}
