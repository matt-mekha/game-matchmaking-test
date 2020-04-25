cd ../../Java/WorkingDirectory
java -jar MatchmakingServer.jar &
cd ../../Scripts/Shell
./client.sh $1 $2
exit