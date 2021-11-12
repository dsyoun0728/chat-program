package server.worker;

import packet.ResponsePacket;
import server.Client;
import server.Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;

public class SendTextWorker implements Worker{
    private Client client;

    public SendTextWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        String contentsStr = this.client.getUserNick() + "> ";
        try {
            contentsStr += new String(this.client.getRequestParser().getContents(this.client.getRequestPacketList()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("SendTextWorker IOException\n\n\n");
            e.printStackTrace();
            Worker.handleClientOut(this.client);
        }

        System.out.println(contentsStr);

        for (Client c : Server.getClientList()) {
            if (!c.equals(this.client)) {
                ResponsePacket responsePacket = new ResponsePacket(
                        (byte) 20,
                        (byte) 4,
                        contentsStr.getBytes(StandardCharsets.UTF_8),
                        this.client.getUserNick().getBytes(StandardCharsets.UTF_8)
                );
                c.setResponsePacketList(responsePacket.responsePacketList);
            } else {
                try {
                    ResponsePacket responsePacket = new ResponsePacket(
                            (byte) 20,
                            (byte) 4,
                            "".getBytes(StandardCharsets.UTF_8),
                            this.client.getUserNick().getBytes(StandardCharsets.UTF_8)
                    );
                    this.client.setResponsePacketList(responsePacket.responsePacketList);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            c.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
        }
        Server.getCallback().completed(null, null);
    }
}
