package io.quarkus.deployment.runnerjar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.bootstrap.BootstrapConstants;
import io.quarkus.bootstrap.app.AugmentAction;
import io.quarkus.bootstrap.app.AugmentResult;
import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.bootstrap.model.ApplicationModel;
import io.quarkus.bootstrap.resolver.BootstrapTestBase;
import io.quarkus.bootstrap.resolver.TsArtifact;
import io.quarkus.bootstrap.resolver.TsArtifact.ContentProvider;
import io.quarkus.bootstrap.resolver.TsDependency;
import io.quarkus.builder.BuildResult;
import io.quarkus.maven.dependency.ArtifactDependency;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.maven.dependency.ResolvedDependency;

public abstract class PackageAppTestBase extends BootstrapTestBase {

    private static final String LIB_BOOT_PREFIX = "lib/boot/";
    private static final String MAIN_CLS = "io.quarkus.bootstrap.runner.QuarkusEntryPoint";

    protected List<String> expectedLib = new ArrayList<>();
    protected TsDependency platformDescriptor;
    protected TsDependency platformPropsDep;

    protected void addToExpectedLib(TsArtifact entry) {
        expectedLib.add(entry.getGroupId() + '.' + entry.getArtifactId() + '-' + entry.getVersion() + '.' + entry.getType());
    }

    protected void assertAppModel(ApplicationModel appModel) throws Exception {
    }

    protected String[] expectedExtensionDependencies() {
        return null;
    }

    protected TsDependency platformDescriptor() {
        if (platformDescriptor == null) {
            TsArtifact platformDescr = new TsArtifact("org.acme",
                    "acme" + BootstrapConstants.PLATFORM_DESCRIPTOR_ARTIFACT_ID_SUFFIX, "1.0", "json",
                    "1.0");
            platformDescr.setContent(new ContentProvider() {
                Path platformJson;

                @Override
                public Path getPath(Path workDir) throws IOException {
                    if (platformJson == null) {
                        platformJson = workDir.resolve("platform-descriptor.json");
                        try (BufferedWriter writer = Files.newBufferedWriter(platformJson)) {
                            writer.write("platform descriptor");
                        }
                    }
                    return platformJson;
                }
            });
            platformDescr.install(repo);
            platformDescriptor = new TsDependency(platformDescr);
        }
        return platformDescriptor;
    }

    protected TsDependency platformProperties() {
        if (platformPropsDep == null) {
            TsArtifact platformProps = new TsArtifact("org.acme",
                    "acme" + BootstrapConstants.PLATFORM_PROPERTIES_ARTIFACT_ID_SUFFIX, null, "properties",
                    "1.0");
            platformProps.setContent(new ContentProvider() {
                Path propsFile;

                @Override
                public Path getPath(Path workDir) throws IOException {
                    if (propsFile == null) {
                        Properties props = new Properties();
                        props.setProperty("platform.quarkus.native.builder-image", "builder-image-url");
                        propsFile = workDir.resolve("platform-properties.properties");
                        try (OutputStream os = Files.newOutputStream(propsFile)) {
                            props.store(os, "Test Quarkus platform properties");
                        }
                    }
                    return propsFile;
                }
            });
            platformProps.install(repo);
            platformPropsDep = new TsDependency(platformProps);
        }
        return platformPropsDep;
    }

    public static Collection<Dependency> getDeploymentOnlyDeps(ApplicationModel model) {
        return model.getDependencies().stream().filter(d -> d.isDeploymentCp() && !d.isRuntimeCp())
                .map(ArtifactDependency::new).collect(Collectors.toSet());
    }

    public static Collection<Dependency> getDependenciesWithFlag(ApplicationModel model, int flag) {
        var set = new HashSet<Dependency>();
        for (var d : model.getDependencies(flag)) {
            set.add(new ArtifactDependency(d));
        }
        return set;
    }

    public static Collection<Dependency> getDependenciesWithAnyFlag(ApplicationModel model, int... flags) {
        var set = new HashSet<Dependency>();
        for (var d : model.getDependenciesWithAnyFlag(flags)) {
            set.add(new ArtifactDependency(d));
        }
        return set;
    }

    /**
     * Overriding this method allows performing a custom build targeting specific build items.
     *
     * @return alternative build items that should be produced instead of the default packaging ones
     */
    protected Class<?>[] targetBuildItems() {
        return null;
    }

    private AugmentAction initAugmentAction(QuarkusBootstrap creator) throws Exception {
        final CuratedApplication curated = creator.bootstrap();
        assertAppModel(curated.getApplicationModel());
        final String[] expectedExtensions = expectedExtensionDependencies();
        if (expectedExtensions != null) {
            assertExtensionDependencies(curated.getApplicationModel(), expectedExtensions);
        }
        return curated.createAugmentor();
    }

    @Override
    protected void testBootstrap(QuarkusBootstrap bootstrap) throws Exception {
        testAugmentResult(initAugmentAction(bootstrap));
    }

    protected void testAugmentResult(AugmentAction augmentAction) throws Exception {
        final Class<?>[] targetBuildItems = targetBuildItems();
        if (targetBuildItems == null) {
            assertAugmentOutcome(augmentAction.createProductionApplication());
        } else {
            augmentAction.performCustomBuild(CustomBuildConsumer.class.getName(), this, toClassNames(targetBuildItems));
        }
    }

    private static String[] toClassNames(Class<?>[] classes) {
        final String[] arr = new String[classes.length];
        for (int i = 0; i < classes.length; ++i) {
            arr[i] = classes[i].getName();
        }
        return arr;
    }

    public static class CustomBuildConsumer implements BiConsumer<PackageAppTestBase, BuildResult> {
        @Override
        public void accept(PackageAppTestBase test, BuildResult buildResult) {
            test.assertBuildResult(buildResult);
        }
    }

    protected void assertBuildResult(BuildResult result) {
    }

    private void assertAugmentOutcome(AugmentResult outcome) throws IOException {
        final Path libDir = outcome.getJar().getLibraryDir();
        assertTrue(Files.isDirectory(libDir));
        final Path bootLibDir = libDir.resolve("boot");
        assertTrue(Files.isDirectory(bootLibDir));
        final Set<String> actualBootLib = getDirContent(bootLibDir);
        final Path mainLibDir = libDir.resolve("main");
        assertTrue(Files.isDirectory(mainLibDir));
        final Set<String> actualMainLib = getDirContent(mainLibDir);

        final Path runnerJar = outcome.getJar().getPath();
        assertTrue(Files.exists(runnerJar));
        try (JarFile jar = new JarFile(runnerJar.toFile())) {
            final Attributes mainAttrs = jar.getManifest().getMainAttributes();

            // assert the main class
            assertEquals(MAIN_CLS, mainAttrs.getValue("Main-Class"));

            // assert the Class-Path contains all the entries in the lib dir
            final String cp = mainAttrs.getValue("Class-Path");
            assertNotNull(cp);
            String[] cpEntries = Arrays.stream(cp.trim().split("\\s+"))
                    .filter(s -> !s.trim().isEmpty())
                    .toArray(String[]::new);
            assertEquals(actualBootLib.size(), cpEntries.length);
            for (String entry : cpEntries) {
                assertThat(entry).startsWith(LIB_BOOT_PREFIX);
                String entryFile = entry.substring(LIB_BOOT_PREFIX.length());
                assertThat(actualBootLib).contains(entryFile);
            }
        }
        assertLibDirectoryContent(actualMainLib);
    }

    protected void assertLibDirectoryContent(Set<String> actualMainLib) {
        assertThat(actualMainLib).containsExactlyInAnyOrderElementsOf(expectedLib);
    }

    protected Set<String> getDirContent(final Path dir) throws IOException {
        final Set<String> actualBootLib = new HashSet<>();
        try (Stream<Path> stream = Files.list(dir)) {
            final Iterator<Path> i = stream.iterator();
            while (i.hasNext()) {
                actualBootLib.add(i.next().getFileName().toString());
            }
        }
        return actualBootLib;
    }

    private static void assertExtensionDependencies(ApplicationModel appModel, String[] expectedExtensions) {
        final Set<String> expectedRuntime = new HashSet<>(expectedExtensions.length);
        final Set<String> expectedDeployment = new HashSet<>(expectedExtensions.length);
        for (String rtId : expectedExtensions) {
            expectedRuntime.add(TsArtifact.DEFAULT_GROUP_ID + ":" + rtId + "::jar:" + TsArtifact.DEFAULT_VERSION);
            expectedDeployment
                    .add(TsArtifact.DEFAULT_GROUP_ID + ":" + rtId + "-deployment" + "::jar:" + TsArtifact.DEFAULT_VERSION);
        }

        final Collection<ResolvedDependency> rtDeps = appModel.getRuntimeDependencies();
        for (Dependency dep : rtDeps) {
            final String coords = dep.toGACTVString();
            assertTrue(expectedRuntime.contains(coords), coords);
        }
        assertEquals(expectedExtensions.length, rtDeps.size());

        final List<Dependency> deploymentOnly = appModel.getDependencies().stream()
                .filter(d -> d.isDeploymentCp() && !d.isRuntimeCp()).collect(Collectors.toList());
        for (Dependency dep : deploymentOnly) {
            final String coords = dep.toGACTVString();
            assertTrue(expectedDeployment.contains(coords), coords);
        }
        assertEquals(expectedExtensions.length, deploymentOnly.size());
    }
}
