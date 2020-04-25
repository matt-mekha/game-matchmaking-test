cd ../../Java/WorkingDirectory
java -jar MatchmakingServer.jar &
cd ../../Scripts/Shell
for i in `seq $1`; do
    ./client.sh &
done
exit