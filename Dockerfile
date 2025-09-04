FROM openjdk:16-jdk-alpine

ARG JYTHON_VERSION=2.7.0
ARG JYTHON_HOME=/usr/src/jython-$JYTHON_VERSION

ENV JYTHON_VERSION=$JYTHON_VERSION
ENV JYTHON_HOME=$JYTHON_HOME
ENV PATH=$PATH:$JYTHON_HOME/bin

RUN apk update

RUN set -eux && \
    apk add --no-cache bash wget zip apache-ant fontconfig ttf-dejavu openjdk8 && \
    ln -sf /bin/bash /bin/sh

WORKDIR /usr/src

RUN wget -cO ivy.zip https://dlcdn.apache.org/ant/ivy/2.5.3/apache-ivy-2.5.3-bin-with-deps.zip && \
    unzip ivy.zip && \
    rm -f ivy.zip

WORKDIR /usr/src/app
COPY . .

RUN ant compile -lib ../apache-ivy-2.5.3/ivy-2.5.3.jar
RUN ant jar -lib ../apache-ivy-2.5.3/ivy-2.5.3.jar

WORKDIR /usr/src/app/
