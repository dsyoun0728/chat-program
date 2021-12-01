package server.worker;

import packet.ResponsePacket;
import server.Client;
import server.Server;
import util.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public interface Worker {
    void doWork();

    static void createWriteQueue(Client client, ArrayList<byte[]> packetList) {
        for (byte[] packet : packetList) {
            CallableMaker callableMaker = new CallableMaker(client, packet);
            FutureTask<Integer> futureTask = new FutureTask(callableMaker);
            Server.getQueue().offer(futureTask);
            Server.getSelector().wakeup();
            try {
                while (futureTask.get() < Constants.PACKET_TOTAL_SIZE) {
                    callableMaker.setByteCount(futureTask.get());
                    futureTask = new FutureTask(callableMaker);
                    Server.getQueue().offer(futureTask);
                    Server.getSelector().wakeup();
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
                    createWriteQueue(c, responsePacket.responsePacketList);
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
