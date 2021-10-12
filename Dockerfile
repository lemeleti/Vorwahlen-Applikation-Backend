FROM openjdk:17-slim-bullseye
COPY ./build/libs/vorwahlen*.jar /app/vorwahlen.jar
WORKDIR /app

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y locales
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen
ENV LANG de_CH.UTF-8
ENV LANGUAGE de_CH:de
ENV LC_ALL de_CH.UTF-8

ENV SQL_HOST=""
ENV SQL_USER=""
ENV SQL_PASSWORD=""
ENV SQL_DB=""

CMD ["java", "-jar", "vorwahlen.jar"]