FROM amd64/clojure:openjdk-17-lein-buster AS build-jar
WORKDIR /knotlog

COPY project.clj /knotlog/
RUN lein deps

COPY . .
RUN lein uberjar

FROM amd64/amazoncorretto:17-alpine
WORKDIR /knotlog

ARG DOCKER_TAG=latest
ENV APP_VERSION=$DOCKER_TAG

RUN echo "Building Docker image version: $APP_VERSION"

COPY --from=build-jar "/knotlog/target/knotlog-*-standalone.jar" knotlog.jar

ENTRYPOINT ["java", "-jar", "/knotlog/knotlog.jar"]
