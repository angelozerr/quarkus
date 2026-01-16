package io.quarkus.qute.debug.breakpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.debug.RenderTemplateInThread;
import io.quarkus.qute.debug.adapter.RegisterDebugServerAdapter;
import io.quarkus.qute.debug.client.DAPClient;
import io.quarkus.qute.debug.client.DebuggerUtils;
import io.quarkus.qute.debug.java.JavaProject;
import io.quarkus.qute.debug.java.JavaSource;
import io.quarkus.qute.debug.java.app.QuarkusApp;
import io.quarkus.qute.debug.java.ide.IDEJavaSourceResolver;

public class JavaAnnotationBreakpointTest {

	@Test
	public void testTemplateRenderingWithDebuggerSimulation() throws Exception {
		int debugPort = DebuggerUtils.findAvailableSocketPort();

		// Create Java project and source
		JavaProject project = new JavaProject();
		project.addSource("""
				package org.acme;

				import io.quarkus.qute.TemplateContents;

				public class HelloResource {
				    @TemplateContents("Hello {name}!")
				    record Hello(String name) {}
				}
				""");

		// Create Quarkus application which indexes Java sources and collect template
		// from @TemplateContents
		QuarkusApp app = new QuarkusApp(project, builder -> {
			builder //
					.enableTracing(true) // enable tracing required by debugger
					.addEngineListener(new RegisterDebugServerAdapter(debugPort, false)); // debug engine on the given
																							// port
		});

		// Create IDE resolver which simulate IDE/Editors (IntelliJ/vscode) to parse
		// Java files
		// and collect start line of @TemplateContents
		IDEJavaSourceResolver resolver = new IDEJavaSourceResolver(project);

		// Connect DAPClient
		DAPClient client = new DAPClient();
		client.setJavaFileInfoProvider(resolver);
		client.connectToServer(debugPort).get(5000, TimeUnit.MILLISECONDS);

		// 7️. Render '@TemplateContents("Hello {name}!")' template normally
		StringBuilder renderResult = new StringBuilder();
		RenderTemplateInThread renderThread = app.render(
				"qute-java://org.acme.HelloResource$Hello@io.quarkus.qute.TemplateContents", renderResult, instance -> {
					instance.data("name", "Quarkus");
				});
		assertEquals("Hello Quarkus!", renderResult.toString());

		// 8️. Set breakpoint on template line (simulated)
		client.setBreakpoint("/home/HelloResource.java", 6);

		// 9️⃣ Render template with breakpoint
		renderResult.setLength(0);
		renderThread.render();

		// 10️. While paused on breakpoint, the result should be empty
		assertEquals("", renderResult.toString());

		// 11️⃣ Inspect debug thread
		var threads = client.getThreads();
		assertEquals(1, threads.length);

		var thread = threads[0];
		int threadId = thread.getId();
		assertEquals("Qute render thread", thread.getName());

		// 12️⃣ Inspect stack frames
		var stackFrames = client.getStackFrames(threadId);
		var currentFrame = stackFrames[0];
		int frameId = currentFrame.getId();
		String frameName = currentFrame.getName();
		assertFalse(frameName.isEmpty());

		// 13️⃣ Inspect variables in Globals scope
		var scopes = client.getScopes(frameId);
		assertFalse(scopes.length == 0);
		var globalsScope = scopes[1];

		var variables = client.getVariables(globalsScope.getVariablesReference());
		assertEquals(1, variables.length);
		assertEquals("name", variables[0].getName());
		assertEquals("Quarkus", variables[0].getValue());

		// 14️⃣ Resume the thread to finish rendering
		client.resume(threadId);
		Thread.sleep(500); // wait for render to complete
		assertEquals("Hello Quarkus!", renderResult.toString());

		// 15️⃣ Cleanup
		client.terminate();
	}
}
