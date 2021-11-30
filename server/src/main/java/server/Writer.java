package server;

/*
import parser.Parser;
import server.worker.Worker;
import util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;


public class Writer {
    private Client client;

    static void createWriteRunnable(Client client, ArrayList<byte[]> packetList) {
        Runnable writeRunnable = () -> {
            UUID uuid = Parser.getUUID(packetList.get(0));
            try {
                for (byte[] packet : packetList) {
                    int sendCount = 0;
                    client.getWriteByteBuffer().clear();
                    client.getWriteByteBuffer().put(packet);
                    client.getWriteByteBuffer().flip();
                    while (sendCount < Constants.PACKET_TOTAL_SIZE) {
                        sendCount += client.getSocketChannel().write(client.getWriteByteBuffer());
                    }
                    client.getWriteByteBuffer().clear();
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
        Server.getExecutorService().submit(writeRunnable);
    }
}*/
