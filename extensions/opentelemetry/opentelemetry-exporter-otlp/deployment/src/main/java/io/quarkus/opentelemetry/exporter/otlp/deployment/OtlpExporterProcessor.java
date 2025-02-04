package io.quarkus.opentelemetry.exporter.otlp.deployment;

import java.util.function.BooleanSupplier;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Feature;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.opentelemetry.exporter.otlp.runtime.OtlpExporterConfig;
import io.quarkus.opentelemetry.exporter.otlp.runtime.OtlpExporterProvider;
import io.quarkus.opentelemetry.exporter.otlp.runtime.OtlpRecorder;

@BuildSteps(onlyIf = OtlpExporterProcessor.OtlpExporterEnabled.class)
public class OtlpExporterProcessor {

    static class OtlpExporterEnabled implements BooleanSupplier {
        OtlpExporterConfig.OtlpExporterBuildConfig otlpExporterConfig;

        public boolean getAsBoolean() {
            return otlpExporterConfig.enabled;
        }
    }

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(Feature.OPENTELEMETRY_OTLP_EXPORTER);
    }

    @BuildStep
    AdditionalBeanBuildItem createBatchSpanProcessor() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(OtlpExporterProvider.class)
                .setUnremovable().build();
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void installBatchSpanProcessorForOtlp(OtlpRecorder recorder,
            LaunchModeBuildItem launchModeBuildItem,
            OtlpExporterConfig.OtlpExporterRuntimeConfig runtimeConfig) {
        recorder.installBatchSpanProcessorForOtlp(runtimeConfig, launchModeBuildItem.getLaunchMode());
    }
}
