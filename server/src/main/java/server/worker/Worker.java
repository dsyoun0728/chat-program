package server.worker;

import packet.ResponsePacket;
import server.Client;
import server.Server;
import util.Constants;
import util.Function;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public interface Worker {
    void doWork();

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
                    c.setResponsePacketList(uuid, responsePacket.responsePacketList);
                    c.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
                } else {
                    c.getSelectionKey().interestOps(SelectionKey.OP_READ);
                }
                c.clearRequestPacketList(uuid);
                c.getSocketChannel().close();
                Server.setClientList(false, client);
                Server.getCallback().completed(null, null);
            }
        } catch (IOException e) {
            System.out.println("socketChannel close IOException\n\n\n");
            Server.getCallback().failed(e, null);
            e.printStackTrace();
        }
    }
}
