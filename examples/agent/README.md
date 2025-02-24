# Lightstep Agent Example

## Build and Run
```shell script
export LS_ACCESS_TOKEN=your-token
export LS_SERVICE_NAME=your-service-name
export OTEL_EXPORTER_OTLP_SPAN_ENDPOINT=ingest.lightstep.com

make buid
make run
```

## Manual instrumentation

1. Added next dependencies:
    ```xml
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-extension-annotations</artifactId>
        <version>0.14.1</version>
    </dependency>

    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-api</artifactId>
        <version>0.14.1</version>
    </dependency>
    ```

1. There are two ways to create spans:

    1. Any method can be annotated with `@WithSpan` annotation to create span with duration of method call
        ```java
        @WithSpan
        public void method() {
          // ...
        }
        ```

    1. Manually get tracer and create span
        ```java
        Tracer tracer = OpenTelemetry.getGlobalTracer(System.getenv("LS_SERVICE_NAME"));
        Span span = tracer.spanBuilder("name").startSpan();
        span.end();
        ```

