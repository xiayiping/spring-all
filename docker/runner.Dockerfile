FROM maven:3.8.3-openjdk-17
#FROM azul/zulu-openjdk:17-latest
RUN apt update
RUN apt install maven -y

# RUN apt-get install -y curl \
#   && curl -sL https://deb.nodesource.com/setup_9.x | bash - \
#   && apt-get install -y nodejs \
#   && curl -L https://www.npmjs.com/install.sh | sh \
# RUN npm install -g grunt grunt-cli
#
# RUN npm install @semantic-release/git @semantic-release/gitlab @semantic-release/exec
