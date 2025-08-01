package io.quarkus.maven.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.logging.Logger;

import io.quarkus.devtools.testing.RegistryClientTestHelper;
import io.quarkus.test.devmode.util.DevModeClient;

public class MojoTestBase {

    private static final Logger LOGGER = Logger.getLogger(MojoTestBase.class);

    public Invoker initInvoker(File root) {
        Invoker invoker = new DefaultInvoker() {
            @Override
            public InvocationResult execute(InvocationRequest request)
                    throws MavenInvocationException {
                passUserSettings(request);
                getEnv().forEach(request::addShellEnvironment);
                enableDevToolsTestConfig(request);
                return super.execute(request);
            }
        };
        invoker.setWorkingDirectory(root);
        String repo = System.getProperty("maven.repo.local");
        if (repo == null) {
            repo = new File(System.getProperty("user.home"), ".m2/repository").getAbsolutePath();
        }
        invoker.setLocalRepositoryDirectory(new File(repo));
        return invoker;
    }

    public static void passUserSettings(InvocationRequest request) {
        final String mvnSettings = System.getProperty("maven.settings");
        if (mvnSettings != null) {
            final File settingsFile = new File(mvnSettings);
            if (settingsFile.exists()) {
                request.setUserSettingsFile(settingsFile);
            }
        }
    }

    public static File initEmptyProject(String name) {
        File tc = new File("target/test-classes/" + name);
        if (tc.isDirectory()) {
            try {
                FileUtils.deleteDirectory(tc);
            } catch (IOException e) {
                throw new RuntimeException("Cannot delete directory: " + tc, e);
            }
        }
        boolean mkdirs = tc.mkdirs();

        initDotMvn();

        LOGGER.debugf("test-classes created? %s", mkdirs);
        return tc;
    }

    public static File initProject(String name) {
        File tc = new File("target/test-classes");
        if (!tc.isDirectory()) {
            boolean mkdirs = tc.mkdirs();
            LOGGER.debugf("test-classes created? %s", mkdirs);
        }

        File in = new File(tc, name);
        if (!in.isDirectory()) {
            throw new RuntimeException("Cannot find directory: " + in.getAbsolutePath());
        }

        initDotMvn();

        return in;
    }

    public static File getTargetDir(String name) {
        return new File("target/test-classes/" + name);
    }

    public static File initProject(String name, String output) {
        File tc = new File("target/test-classes");
        if (!tc.isDirectory()) {
            boolean mkdirs = tc.mkdirs();
            LOGGER.debugf("test-classes created? %s", mkdirs);
        }

        File in = new File(tc, name);
        if (!in.isDirectory()) {
            throw new RuntimeException("Cannot find directory: " + in.getAbsolutePath());
        }

        File out = new File(tc, output);
        if (out.isDirectory()) {
            FileUtils.deleteQuietly(out);
        }
        boolean mkdirs = out.mkdirs();
        LOGGER.debugf(out.getAbsolutePath() + " created? %s", mkdirs);

        initDotMvn();

        try {
            org.codehaus.plexus.util.FileUtils.copyDirectoryStructure(in, out);
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy project resources", e);
        }

        return out;
    }

    /**
     * Initialize an empty .mvn to make sure we don't inherit from .mvn/maven.config from the root
     * <p>
     * .mvn/maven.config is used to pass args that might be too long for the Windows command line.
     */
    private static void initDotMvn() {
        File dotMvn = new File("target/.mvn");
        dotMvn.mkdirs();
    }

    public static void filter(File input, Map<String, String> variables) throws IOException {
        DevModeClient.filter(input, variables);
    }

    public Map<String, String> getEnv() {
        String opts = System.getProperty("mavenOpts");
        Map<String, String> env = new HashMap<>();
        if (opts != null) {
            env.put("MAVEN_OPTS", opts);
        }
        return env;
    }

    public static void assertThatOutputWorksCorrectly(String logs) {
        assertThat(logs.isEmpty()).isFalse();
        String infoLogLevel = "INFO";
        assertThat(logs).contains(infoLogLevel);
        assertThat(logs).containsPattern("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d{3}");
        assertThat(logs).contains("cdi, rest, smallrye-context-propagation, vertx, websockets");
        assertThat(logs).doesNotContain("JBoss Threads version");
    }

    public static Model loadPom(File directory) {
        File pom = new File(directory, "pom.xml");
        assertThat(pom).isFile();
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(pom), StandardCharsets.UTF_8);) {
            return new MavenXpp3Reader().read(isr);
        } catch (IOException | XmlPullParserException e) {
            throw new IllegalArgumentException("Cannot read the pom.xml file", e);
        }
    }

    public static List<File> getFilesEndingWith(File dir, String suffix) {
        final File[] files = dir.listFiles((d, name) -> name.endsWith(suffix));
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    public static void enableDevToolsTestConfig(InvocationRequest request) {
        Properties properties = request.getProperties();
        if (properties == null) {
            properties = new Properties();
            request.setProperties(properties);
        }
        enableDevToolsTestConfig(properties);
    }

    public static void enableDevToolsTestConfig(Properties properties) {
        RegistryClientTestHelper.enableRegistryClientTestConfig(properties);
    }

    public static void disableDevToolsTestConfig(Properties properties) {
        RegistryClientTestHelper.disableRegistryClientTestConfig(properties);
    }
}
