package server.worker;

import server.Client;

public class DeleteFileWorker implements Worker {
    private Client client;

    public DeleteFileWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        System.out.println("DeleteFileWorker");
        System.out.println(client);
    }
}
