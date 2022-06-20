package io.quarkus.qute.debug.adapter;

import io.quarkus.qute.Engine;
import io.quarkus.qute.EngineBuilder.EngineListener;
import io.quarkus.qute.debug.agent.DebuggeeAgent;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RegisterDebugServerAdapter3 implements EngineListener {

    private DebuggeeAgent agent;
    private volatile ServerSocket serverSocket;
    private Future<Void> future;

    @Override
    public void engineBuilt(Engine engine) {
        if (agent == null) {
            agent = new DebuggeeAgent();
            int port = 4711;
            boolean waitForClientConnect = false;
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("DebugServerAdapter listening on port " + port);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Socket client = null;
            if (waitForClientConnect) {
                // blocking call
                try {
                    client = serverSocket.accept();

                    var server = new DebugServerAdapter(agent);
                    Launcher<IDebugProtocolClient> launcher = DSPLauncher.createServerLauncher(
                            server,
                            client.getInputStream(),
                            client.getOutputStream()
                    );

                    var clientProxy = launcher.getRemoteProxy();
                    server.connect(clientProxy);

                    Thread t = new Thread(() -> {
                        try {
                            future = launcher.startListening();
                            future.get();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (ExecutionException e) {
                          throw new RuntimeException(e);
                        }
                    });
                    t.setDaemon(true);
                    t.start();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // run in daemon thread
                final Socket cl = client;
                Thread t = new Thread(() -> run(cl));
                t.setDaemon(true);
                t.start();
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (future != null) {
                    future.cancel(true);
                }
                System.out.println("Shutdown hook: closing server socket.");
                try {

                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }

        agent.track(engine);
    }

    private void run(Socket client) {
        try (ServerSocket ss = serverSocket) {
            if (client == null) {
                client = ss.accept();
                System.out.println("DAP client connected from " + client.getInetAddress());
            }

            var server = new DebugServerAdapter(agent);
            Launcher<IDebugProtocolClient> launcher = DSPLauncher.createServerLauncher(
                    server,
                    client.getInputStream(),
                    client.getOutputStream()
            );

            var clientProxy = launcher.getRemoteProxy();
            server.connect(clientProxy);

            launcher.startListening().get();

            System.out.println("DAP client disconnected.");

        } catch (IOException e) {
            if (serverSocket.isClosed()) {
                System.out.println("Server socket closed, stopping listener.");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("DebugServerAdapter stopped listening.");
        }

    }

}
