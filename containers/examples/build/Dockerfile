FROM busybox

WORKDIR /opt/

USER root

RUN ["mkdir", "-p", "/opt"]

COPY messages /opt/messages

RUN echo "additional message" >> /opt/messages

CMD ["/bin/head", "-n", "10", "/opt/messages"]
