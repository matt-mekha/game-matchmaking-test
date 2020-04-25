cd ../../Java
mkdir -p WorkingDirectory
rm -r WorkingDirectory/*

Client/build.sh &
Servers/MatchmakingServer/build.sh &
Servers/GameServer/build.sh &
wait

cd ../Scripts/Shell