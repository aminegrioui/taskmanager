FROM openjdk:21
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","-Djava.security.auth.login.config=/app/META-INF/.java.login.config", "-Djava.security.egd=file:/dev/urandom" ,"com.aminejava.taskmanager.TaskManagerApplication"]
ENV TZ="Europe/Berlin"