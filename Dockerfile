FROM openjdk:11

RUN mkdir -p /ws/xcal/app
COPY target/web-api-service-main.jar /ws/xcal/app
WORKDIR /ws/xcal/app

ENTRYPOINT java ${JAVA_OPTS} -jar web-api-service-main.jar