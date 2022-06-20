package io.quarkus.qute.debug.adapter;

import io.quarkus.qute.Engine;
import io.quarkus.qute.EngineBuilder.EngineListener;
import io.quarkus.qute.debug.agent.DebuggeeAgent;
import io.quarkus.qute.trace.ResolveEvent;
import io.quarkus.qute.trace.TemplateEvent;
import io.quarkus.qute.trace.TraceListener;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.debug.DebugLauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class RegisterDebugServerAdapter4 implements EngineListener {

    private DebuggeeAgent agent;
    private volatile ServerSocket serverSocket;
    private Future<Void> future;
    private DebugServerAdapter server;

    @Override
    public void engineBuilt(Engine engine) {

        if (agent != null) {
            agent.track(engine);
            return;
        }
        agent  = new DebuggeeAgent();
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






        /*if (agent == null) {
            agent = new DebuggeeAgent();
            agent.track(engine);



            int port = 4711;
            boolean waitForClientConnect = false;
            try (var serverSocket = new ServerSocket(port)) {
                System.out.println("DebugServerAdapter listening on port " + port);
                var client = serverSocket.accept();

                var server = new DebugServerAdapter(agent);
                Launcher<IDebugProtocolClient> launcher = DSPLauncher.createServerLauncher(
                        server,
                        client.getInputStream(),
                        client.getOutputStream()
                );

       //         CompletableFuture.runAsync(() -> {


                    var clientProxy = launcher.getRemoteProxy();
                    server.connect(clientProxy);
                    launcher.startListening();

         //       });

            } catch (IOException e) {
                e.printStackTrace();
                return;
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
        } else {
            agent.track(engine);
        }
*/

    }

    private synchronized void initializeAgent() {
        int port = 4711;
        boolean waitForClientConnect = false;
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("DebugServerAdapter listening on port " + port);
            var client = serverSocket.accept();

            UnaryOperator<MessageConsumer> wrapper = consumer -> (message -> {
                CompletableFuture.runAsync(() -> consumer.consume(message));
            });
            
            Launcher<IDebugProtocolClient> launcher = createLauncher(
                    server,
                    IDebugProtocolClient.class,
                    client.getInputStream(),
                    client.getOutputStream(),
                    wrapper
            );

            //         CompletableFuture.runAsync(() -> {



            var t = new Thread(() -> {
                var clientProxy = launcher.getRemoteProxy();
                server.connect(clientProxy);
                launcher.startListening();
            });
            t.setDaemon(true);
            t.start();

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
        }
        //this.agent = agent;
    }


    private static <T> Launcher<T> createLauncher(Object localService, Class<T> remoteInterface, InputStream in,
                                                 OutputStream out, Function<MessageConsumer, MessageConsumer> wrapper) {
        return new DebugLauncher.Builder<T>()
                .setLocalService(localService)
                .setRemoteInterface(remoteInterface)
                .setInput(in)
                .setOutput(out)
                .wrapMessages(wrapper)
                .create();
    }
}
