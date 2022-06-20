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

public class RegisterDebugServerAdapter2 implements EngineListener {

    private DebuggeeAgent agent;
    private volatile ServerSocket serverSocket;
    private Future<Void> future;

    @Override
    public void engineBuilt(Engine engine) {
        if (agent == null) {
            agent = new DebuggeeAgent();
            int port = 4711;
            try (ServerSocket ss = serverSocket) {
                    var client = ss.accept();
                    System.out.println("DAP client connected from " + client.getInetAddress());

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
        agent.track(engine);
    }

}
