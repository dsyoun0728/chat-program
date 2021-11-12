package server.worker;

import server.Client;

public class DeleteFilesWorker implements Worker{
    private Client client;

    public DeleteFilesWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        System.out.println("DeleteFilesWorker");
        System.out.println(client);
    }
}
