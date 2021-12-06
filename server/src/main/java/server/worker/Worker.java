package server.worker;

import packet.ResponsePacket;
import server.Client;
import server.Server;
import util.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public interface Worker {
    void doWork();

    static void createWriteRunnable(Client client, ArrayList<byte[]> packetList) {
        RunnableMaker runnableMaker = new RunnableMaker(client, packetList);
        Server.getQueue().offer(runnableMaker);
        Server.getSelector().wakeup();
    }

    static void handleClientOut(Client client, UUID uuid) {
        try {
            System.out.println("[클라이언트 통신 안됨: " + client.getSocketChannel().getRemoteAddress() + ": " + Thread.currentThread().getName() + "]");
            for (Client c : Server.getClientList()) {
                if (!c.equals(client)) {
                    ResponsePacket responsePacket = new ResponsePacket(
                            uuid,
                            (byte) Constants.RESPONSE_SUCCESS,
                            "Logout",
                            (client.getUserNick() + "님의 연결이 종료되었습니다.").getBytes(StandardCharsets.UTF_8),
                            "".getBytes(StandardCharsets.UTF_8)
                    );
                    createWriteRunnable(c, responsePacket.responsePacketList);
                }
                client.getSocketChannel().close();
                Server.setClientList(false, client);
            }
        } catch (IOException e) {
            System.out.println("socketChannel close IOException\n\n\n");
            e.printStackTrace();
        }
    }
}
