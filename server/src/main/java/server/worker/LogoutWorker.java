package server.worker;

import packet.ResponsePacket;
import server.Client;
import server.Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;

public class LogoutWorker implements Worker {
    private Client client;

    public LogoutWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        try {
            System.out.println("클라이언트 연결 정상적으로 끊김" + client.getSocketChannel().getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResponsePacket rp = new ResponsePacket(
                (byte) 20,
                (byte) 1,
                "".getBytes(StandardCharsets.UTF_8),
                "".getBytes(StandardCharsets.UTF_8)
        );
        client.setResponsePacketList(rp.responsePacketList);
        client.getSelectionKey().interestOps(SelectionKey.OP_WRITE);

        Server.setClientList(false,client);

        String contentsStr = client.getUserNick()+"님이 로그아웃하셨습니다";

        for (Client c : Server.getClientList()) {
            ResponsePacket responsePacket = new ResponsePacket(
                    (byte) 20,
                    (byte) 4,
                    contentsStr.getBytes(StandardCharsets.UTF_8),
                    "".getBytes(StandardCharsets.UTF_8)
            );
            c.setResponsePacketList(responsePacket.responsePacketList);
            c.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        }
        Server.getCallback().completed(null, null);

    }
}
