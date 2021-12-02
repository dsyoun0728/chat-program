package server.worker;

import server.Client;

import java.io.IOException;

public class FirstWrite extends Thread{
    private Client client;
    private byte[] packet;

    public FirstWrite(Client client, byte[] packet){
        this.client = client;
        this.packet = packet;
    }

    @Override
    public void run(){
        try {
            client.getWriteByteBuffer().clear();
            client.getWriteByteBuffer().put(packet);
            client.getWriteByteBuffer().flip();
            int writeCount = client.getSocketChannel().write(client.getWriteByteBuffer());
            client.setWriteCount(writeCount);
        } catch (IOException e) {
            System.out.println("Writer IOException\t\t\t");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Writer Exception\n\n\n");
            e.printStackTrace();
        }
    }
}