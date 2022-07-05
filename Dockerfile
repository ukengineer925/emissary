# syntax=docker/dockerfile:1.4
FROM maven:3.8.6-amazoncorretto-11 as builder

WORKDIR /tmp/build
COPY emissary emissary
COPY contrib contrib
COPY .gitignore .gitignore
COPY pom.xml ./
COPY .git .git
COPY src src
RUN --mount=type=cache,target=/root/.m2,rw mvn -Pdist -B -q -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

FROM amazoncorretto:11-alpine as runtime

#Static options
ENV PROJECT_BASE=/opt/emissary
VOLUME /opt/emissary/localoutput
EXPOSE 8000 8001
ENTRYPOINT ["./emissary"]
#Default parameters for emissary, can be overridden
CMD ["server", "-a", "2", "-p", "8001"]
WORKDIR /opt/emissary
RUN mkdir localoutput

#Unpack distribution
COPY --from=builder /tmp/build/target/emissary-dist.tar.gz /opt
RUN tar -xvf /opt/emissary-dist.tar.gz
RUN chmod -R a+rw /opt/emissary && chmod +x /opt/emissary/emissary