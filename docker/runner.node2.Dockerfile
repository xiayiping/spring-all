#FROM maven:3.8.3-openjdk-17
FROM runner-node-yipingx

WORKDIR /home/gitlab-runner

RUN apt install -y maven
