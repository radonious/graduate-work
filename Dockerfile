# Build static UI
FROM node:23 AS BUILD_UI
WORKDIR /app
COPY . .
WORKDIR /app/ui
RUN npm install
RUN npm fund
RUN npm run build

# Build app JAR
FROM gradle:8-jdk21-corretto AS BUILD_APP
WORKDIR /app
ARG STATIC=/app/src/main/resources/static
COPY . .
COPY --from=BUILD_UI /app/ui/dist/assets/ ${STATIC}/assets/
COPY --from=BUILD_UI /app/ui/dist/favicon.ico ${STATIC}/
COPY --from=BUILD_UI /app/ui/dist/index.html ${STATIC}/
# First run is slow because full dependance loading takes around 5 min
RUN gradle bootJar

# Run app
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=BUILD_APP /app/.env /app/
COPY --from=BUILD_APP /app/build/libs/*.jar /app/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "plag-0.1.jar"]
