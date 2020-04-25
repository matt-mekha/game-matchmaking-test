cd ../Protos
protoc -I=./ --java_out=../Client/core/src request_client.proto
protoc -I=./ --java_out=../Servers/GameServer/src/main/java request_server.proto