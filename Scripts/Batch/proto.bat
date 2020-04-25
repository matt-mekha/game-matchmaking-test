cd ../../Protos
protoc -I=./ --java_out=../Java/Client/core/src request_client.proto
protoc -I=./ --java_out=../Java/Servers/GameServer/src/main/java request_server.proto
cd ../Scripts/Batch