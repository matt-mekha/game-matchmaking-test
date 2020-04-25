cd ../../Java/WorkingDirectory
java -jar MatchmakingServer.jar &
cd ../../Scripts/Batch
for i in {1..10}; do client &; done
exit