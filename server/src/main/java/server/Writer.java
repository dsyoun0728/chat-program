package server;

import server.Worker.Worker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class Writer {
    private Client client;

    public Writer(Client client) {
        this.client = client;
    }

    public Runnable writeToChannel() {
        Runnable runnable = () -> {
            try {
                for (byte[] packet : this.client.getResponsePacketList()) {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(packet);
                    this.client.getSocketChannel().write(byteBuffer);
                }
            } catch (IOException e) {
                System.out.println("Writer IOException\t\t\t");
                Server.getCallback().failed(e, null);
                e.printStackTrace();
                Worker.handleClientOut(this.client);
            } catch (Exception e) {
                System.out.println("Writer Exception\n\n\n");
                Server.getCallback().failed(e, null);
                e.printStackTrace();
            }
            this.client.clearRequestPacketList();
            this.client.clearResponsePacketList();
            this.client.getSelectionKey().interestOps(SelectionKey.OP_READ);
            Server.getCallback().completed(null, null);
        };
        return runnable;
    }
}
