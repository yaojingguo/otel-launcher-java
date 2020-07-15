package com.lightstep.opentelemetry.exporter;

import com.google.common.collect.ImmutableMap;
import com.lightstep.opentelemetry.common.VariablesConverter;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.exporters.otlp.OtlpGrpcSpanExporter;
import io.opentelemetry.extensions.trace.propagation.B3Propagator;
import io.opentelemetry.extensions.trace.propagation.JaegerPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.trace.propagation.HttpTraceContext;
import java.util.Map;

public class LightstepExporter {

  public static class Builder {
    private String accessToken = "";
    private String satelliteUrl = VariablesConverter.DEFAULT_LS_SATELLITE_URL;
    private long deadlineMillis = VariablesConverter.DEFAULT_LS_DEADLINE_MILLIS;
    private boolean useTransportSecurity = VariablesConverter.DEFAULT_LS_USE_TLS;
    private Propagator propagator = Propagator.valueOfLabel(VariablesConverter.DEFAULT_PROPAGATOR);

    private static final Map<Propagator, HttpTextFormat> PROPAGATORS =
        ImmutableMap.of(
            Propagator.TRACE_CONTEXT,
            new HttpTraceContext(),
            Propagator.B3,
            B3Propagator.getMultipleHeaderPropagator(),
            Propagator.B3_SINGLE,
            B3Propagator.getSingleHeaderPropagator(),
            Propagator.JAEGER,
            new JaegerPropagator());

    /**
     * Sets the token for Lightstep access
     *
     * @param accessToken Your specific token for Lightstep access.
     * @return this builder's instance
     */
    public Builder setAccessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    /**
     * Sets the satellite url
     *
     * @param satelliteUrl satellite url
     * @return this builder's instance
     */
    public Builder setSatelliteUrl(String satelliteUrl) {
      this.satelliteUrl = satelliteUrl;
      return this;
    }

    public Builder setPropagator(Propagator propagator) {
      this.propagator = propagator;
      return this;
    }

    /**
     * Overrides the default deadlineMillis with the provided value.
     *
     * @param deadlineMillis The maximum amount of time the tracer should wait for a response from
     * the collector when sending a report.
     * @return this builder's instance
     */
    public Builder setDeadlineMillis(long deadlineMillis) {
      this.deadlineMillis = deadlineMillis;
      return this;
    }

    public Builder useTransportSecurity(boolean useTransportSecurity) {
      this.useTransportSecurity = useTransportSecurity;
      return this;
    }

    /**
     * Constructs a new instance of the exporter based on the builder's values.
     *
     * @return a new exporter's instance
     */
    public OtlpGrpcSpanExporter build() {
      VariablesConverter.convert(satelliteUrl, useTransportSecurity, deadlineMillis, accessToken,
          propagator.label());

      if (propagator != null) {
        final HttpTextFormat httpTextFormat = PROPAGATORS.get(propagator);
        OpenTelemetry.setPropagators(
            DefaultContextPropagators.builder().addHttpTextFormat(httpTextFormat).build());
      }

      return OtlpGrpcSpanExporter.newBuilder()
          .readSystemProperties()
          .readEnvironmentVariables()
          .build();
    }

    /**
     * Installs exporter into tracer SDK default provider with batching span processor.
     */
    public void install() {
      BatchSpanProcessor spansProcessor = BatchSpanProcessor.newBuilder(this.build()).build();
      OpenTelemetrySdk.getTracerProvider().addSpanProcessor(spansProcessor);
    }

    /**
     * Creates builder from system properties and environmental variables.
     *
     * @return this builder's instance
     */
    public static Builder fromEnv() {
      final Builder builder = new Builder();
      builder.accessToken = VariablesConverter.getAccessToken();
      builder.useTransportSecurity = VariablesConverter.useTransportSecurity();
      builder.deadlineMillis = VariablesConverter.getDeadlineMillis();
      builder.satelliteUrl = VariablesConverter.getSatelliteUrl();
      builder.propagator = Propagator.valueOf(VariablesConverter.getPropagator());

      return builder;
    }

  }

  /**
   * Creates a new builder instance.
   *
   * @return a new instance builder for this exporter
   */
  public static Builder newBuilder() {
    return new Builder();
  }

}