FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 platea
COPY --from=build /src/target/platea-*.jar /app/app.jar
USER platea
ENV PORT=8741
ENV THYMELEAF_CACHE=true
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8741
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
