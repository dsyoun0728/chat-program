package server.worker;

import server.Client;
import server.Server;
import util.Constants;

import java.io.IOException;

public class MoreWrite extends Thread{
    private Client client;

    public MoreWrite(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            int count = client.getSocketChannel().write(client.getWriteByteBuffer());
            client.setWriteCount(client.getWriteCount()+count);

            if (client.getWriteCount() < Constants.PACKET_TOTAL_SIZE){
                Server.getQueue().offer(new MoreWrite(client));
                Server.getSelector().wakeup();
            } else {
                client.getWriteByteBuffer().clear();
                client.setWriteCount(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
