package server.worker;

import server.Client;

import java.util.UUID;

public class DeleteFilesWorker implements Worker{
    private Client client;
    private UUID uuid;

    public DeleteFilesWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        System.out.println("DeleteFilesWorker");
        System.out.println(client);
    }
}
