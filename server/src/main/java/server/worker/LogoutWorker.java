package server.worker;

import packet.ResponsePacket;
import parser.ParsedMsg;
import server.Client;
import server.Server;
import util.Constants;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;

import java.util.UUID;

public class LogoutWorker implements Worker {
    private Client client;
    private UUID uuid;

    public LogoutWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        ParsedMsg parsedMsg = this.client.getRequestParser().parseMessage(this.client.getRequestPacketList(this.uuid));
        try {
            System.out.println("클라이언트 연결 정상적으로 끊김" + this.client.getSocketChannel().getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResponsePacket responsePacket = new ResponsePacket(
                this.uuid,
                (byte) Constants.RESPONSE_SUCCESS,
                parsedMsg.getFunctionName(),
                "로그아웃 성공".getBytes(StandardCharsets.UTF_8),
                "".getBytes(StandardCharsets.UTF_8)
        );
        this.client.setResponsePacketList(this.uuid, responsePacket.responsePacketList);
        this.client.getSelectionKey().interestOps(SelectionKey.OP_WRITE);

        Server.setClientList(false, this.client);

        String contentsStr = client.getUserNick()+"님이 로그아웃하셨습니다";

        for (Client c : Server.getClientList()) {
            ResponsePacket rp = new ResponsePacket(
                    this.uuid,
                    (byte) Constants.RESPONSE_SUCCESS,
                    parsedMsg.getFunctionName(),
                    contentsStr.getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
            c.setResponsePacketList(this.uuid, rp.responsePacketList);
            c.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        }

    }
}
