FROM openjdk:11

WORKDIR /app

COPY ./target/universal/stage .

RUN mkdir todos

RUN touch ./todos/todos.txt

EXPOSE 8080

CMD ["./bin/todo-list-fs"]