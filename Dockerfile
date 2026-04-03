FROM ubuntu:latest
LABEL authors="rla00"

ENTRYPOINT ["top", "-b"]
