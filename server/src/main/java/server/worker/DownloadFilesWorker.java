package server.worker;

import server.Client;

import java.util.UUID;

public class DownloadFilesWorker implements Worker {
    private Client client;
    private UUID uuid;

    public DownloadFilesWorker(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
    }

    @Override
    public void doWork() {
        System.out.println("DownloadFilesWorker");
        System.out.println(client);
    }
}
