JAVA_TOOL_OPTIONS="-javaagent:opentelemetry-javaagent.jar"; OTEL_TRACES_EXPORTER=otlp; OTEL_METRICS_EXPORTER=otlp; OTEL_LOGS_EXPORTER=otlp; OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:5555;

export JAVA_TOOL_OPTIONS="-javaagent:/d/develop/spring-all/opentelemetry-javaagent.jar"
export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=otlp
export OTEL_LOGS_EXPORTER=
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:5555
mvn spring-boot:run -pl demo-caller -Dspring-boot.run.profiles=caller


export JAVA_TOOL_OPTIONS="-javaagent:/d/develop/spring-all/opentelemetry-javaagent.jar"
export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=otlp
export OTEL_LOGS_EXPORTER=
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:5555
mvn spring-boot:run -pl demo-otel -Dspring-boot.run.profiles=otel


export JAVA_TOOL_OPTIONS="-javaagent:/d/develop/spring-all/opentelemetry-javaagent.jar"
export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=otlp
export OTEL_LOGS_EXPORTER=otlp
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:5555
mvn spring-boot:run -pl demo-echo