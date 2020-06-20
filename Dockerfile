FROM oracle/graalvm-ce:20.1.0-java11 as graalvm
RUN gu install native-image

COPY . /home/app/mn-data-pg
WORKDIR /home/app/mn-data-pg

RUN native-image --no-server -cp build/libs/mn-data-pg-*-all.jar

FROM frolvlad/alpine-glibc
RUN apk update && apk add libstdc++
EXPOSE 8080
COPY --from=graalvm /home/app/mn-data-pg/mn-data-pg /app/mn-data-pg
ENTRYPOINT ["/app/mn-data-pg"]
