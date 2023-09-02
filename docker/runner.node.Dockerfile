FROM ubuntu

WORKDIR /home/gitlab-runner

RUN apt update

RUN apt install -y gnupg ca-certificates curl
RUN curl -s https://repos.azul.com/azul-repo.key | gpg --dearmor -o /usr/share/keyrings/azul.gpg
RUN echo "deb [signed-by=/usr/share/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" | tee /etc/apt/sources.list.d/zulu.list

RUN apt update

RUN apt install -y zulu17-jdk
RUN apt install -y maven

RUN curl -sL https://deb.nodesource.com/setup_18.x | bash -
RUN apt -y install nodejs

RUN apt install -y git
RUN npm install -g semantic-release@21.1.1
RUN npm install -g @semantic-release/git @semantic-release/gitlab @semantic-release/exec
