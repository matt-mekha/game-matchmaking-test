cd ../../Java/WorkingDirectory
start "" java -jar MatchmakingServer.jar
cd ../../Scripts/Batch
client %1 %2
exit