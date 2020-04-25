cd ../Java/WorkingDirectory
start "" java -jar MatchmakingServer.jar
cd ../../Scripts
for /l %%N in (1 1 10) do start "" client