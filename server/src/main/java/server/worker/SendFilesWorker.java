package server.worker;

import server.Client;

import java.util.UUID;

public class SendFilesWorker implements Worker {
    private Client client;
    private UUID uuid;

    public SendFilesWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        System.out.println("SendFilesWorker");
        System.out.println(client);
    }
}
