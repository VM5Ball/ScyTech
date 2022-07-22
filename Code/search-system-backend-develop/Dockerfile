FROM openjdk:11
COPY ./security ./security
COPY ./files ./files
COPY ./elasticconfig ./elasticconfig
COPY ./security ./security
COPY ./target/*.jar ./app.jar
CMD  echo "wait elasticsearch" ; sleep 10s ; java -jar ./app.jar
