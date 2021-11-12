package server.worker;

import server.Client;

public class DownloadFilesWorker implements Worker {
    private Client client;

    public DownloadFilesWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        System.out.println("DownloadFilesWorker");
        System.out.println(client);
    }
}
