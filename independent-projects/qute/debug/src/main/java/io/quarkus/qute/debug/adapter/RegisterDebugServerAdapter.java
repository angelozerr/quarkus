package io.quarkus.qute.debug.adapter;

import io.quarkus.qute.Engine;
import io.quarkus.qute.EngineBuilder.EngineListener;
import io.quarkus.qute.debug.agent.DebuggeeAgent;
import io.quarkus.qute.trace.ResolveEvent;
import io.quarkus.qute.trace.TemplateEvent;
import io.quarkus.qute.trace.TraceListener;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.debug.DebugLauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class RegisterDebugServerAdapter implements EngineListener {

    private boolean initialized;

    private DebuggeeAgent agent;
    private volatile ServerSocket serverSocket;
    private Future<Void> future;
    private DebugServerAdapter server;

    @Override
    public void engineBuilt(Engine engine) {
        if (initialized) {
            agent.track(engine);
            return;
        }

        agent = new DebuggeeAgent();
        agent.track(engine);
        server = new DebugServerAdapter(agent);
        
        engine.addTraceListener(new TraceListener() {
            @Override
            public void beforeResolve(ResolveEvent event) {

            }

            @Override
            public void afterResolve(ResolveEvent event) {

            }

            @Override
            public void startTemplate(TemplateEvent event) {
                initializeAgent();
            }

            @Override
            public void endTemplate(TemplateEvent event) {

            }
        });
    }

    private synchronized void initializeAgent() {
        if (initialized) {
            return;
        }

        int port = 4711;
        boolean waitForClientConnect = false;
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("DebugServerAdapter listening on port " + port);
            var client = serverSocket.accept();

            Launcher<IDebugProtocolClient> launcher = DSPLauncher.createServerLauncher(
                    server,
                    client.getInputStream(),
                    client.getOutputStream(),
                    createDaemonExecutor(),
                    null
            );

            var clientProxy = launcher.getRemoteProxy();
            server.connect(clientProxy);

            Thread listenerThread = new Thread(() -> {
                Future<?> listening = launcher.startListening();
            }, "dap-listener");
            listenerThread.setDaemon(false); // important : sinon le programme peut sortir trop tôt
            listenerThread.start();

            Thread.sleep(3000);

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
            //       });

        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //this.agent = agent;
    }

    private ExecutorService createDaemonExecutor() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // 💡 très important
            t.setName("dap-daemon-thread");
            return t;
        });
    }
}
