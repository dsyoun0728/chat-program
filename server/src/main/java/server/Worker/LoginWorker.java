package server.Worker;

import packet.ResponsePacket;
import server.Client;
import server.Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;

public class LoginWorker implements Worker{
    private Client client;

    public LoginWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        try {
            this.client.setUserNick(new String(this.client.getRequestParser().getContents(this.client.getRequestPacketList()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("LoginWorker IOException\n\n\n");
            e.printStackTrace();
            Worker.handleClientOut(this.client);
        }

        // Server에 출력
        System.out.println(this.client.getUserNick() + "\t입장");

        for (Client c : Server.getClientList()) {
            if (!c.equals(this.client)) {
                ResponsePacket responsePacket = new ResponsePacket(
                        (byte) 20,
                        (byte) 0,
                        ("Server > " + this.client.getUserNick() + " 님이 입장하였습니다.").getBytes(StandardCharsets.UTF_8),
                        "1".getBytes(StandardCharsets.UTF_8)
                );
                c.setResponsePacketList(responsePacket.responsePacketList);
            } else {
                ResponsePacket responsePacket = new ResponsePacket(
                        (byte) 20,
                        (byte) 0,
                        ("Server > " + this.client.getUserNick() + " (으)로 로그인 완료").getBytes(StandardCharsets.UTF_8),
                        "1".getBytes(StandardCharsets.UTF_8)
                );
                this.client.setResponsePacketList(responsePacket.responsePacketList);
            }
            c.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        }
        Server.getCallback().completed(null, null);
    }
}
