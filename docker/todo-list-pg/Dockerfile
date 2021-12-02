FROM openjdk:11

WORKDIR /app

COPY ./target/universal/stage .

EXPOSE 8080

CMD ["./bin/todo-list-pg"]