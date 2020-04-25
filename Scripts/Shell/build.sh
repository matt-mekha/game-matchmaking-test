cd ../../Java
rm -r WorkingDirectory/*
cd ../Client
./build.sh &
cd ../Servers/MatchmakingServer
./build.sh &
cd ../GameServer
./build.sh &
cd ../../WorkingDirectory
cd ../../Scripts/Batch
wait