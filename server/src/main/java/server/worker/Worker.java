package server.worker;

import packet.ResponsePacket;
import parser.Parser;
import server.Client;
import server.Server;
import util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public interface Worker {
    void doWork();

    static Runnable createWriteRunnable(Client client, ArrayList<byte[]> packetList) {
        Runnable writeRunnable = () -> {
            UUID uuid = Parser.getUUID(packetList.get(0));
            try {
                for (byte[] packet : packetList) {
                    int byteCount = 0;
                    ByteBuffer byteBuffer = ByteBuffer.wrap(packet);
                    while (byteCount < Constants.PACKET_TOTAL_SIZE) {
                        byteCount += client.getSocketChannel().write(byteBuffer);
                    }
                }
            } catch (IOException e) {
                System.out.println("Writer IOException\t\t\t");
                e.printStackTrace();
                Worker.handleClientOut(client, uuid);
            } catch (Exception e) {
                System.out.println("Writer Exception\n\n\n");
                e.printStackTrace();
            }
        };
        return writeRunnable;
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
