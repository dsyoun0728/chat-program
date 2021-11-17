package server.worker;

import server.Client;

import java.util.UUID;

public class DeleteFileWorker implements Worker {
    private Client client;
    private UUID uuid;

    public DeleteFileWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        System.out.println("DeleteFileWorker");
        System.out.println(client);
    }
}
