FROM java:8-alpine
MAINTAINER Nick Zbinden <me@nzmail.com>

ADD target/blackjack-0.0.1-SNAPSHOT-standalone.jar /blackjack/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/blackjack/app.jar"]
