cd ../../Java/WorkingDirectory
start "" java -jar MatchmakingServer.jar
cd ../../Scripts/Batch
for /l %%N in (1 1 15) do start "" client
exit