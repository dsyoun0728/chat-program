package server.worker;

import server.Client;

import java.util.UUID;

public class SendWhisperWorker implements Worker {
    private Client client;
    private UUID uuid;

    public SendWhisperWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        System.out.println("SendWhisperWorker");
        System.out.println(client);
    }
}
