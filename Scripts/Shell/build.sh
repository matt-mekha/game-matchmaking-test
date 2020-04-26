cd ../../Java
mkdir -p WorkingDirectory
rm -r WorkingDirectory/

cd Client
./build.sh &
cd ../Servers/MatchmakingServer
./build.sh &
cd ../GameServer
./build.sh &
wait

cd ../../../Scripts/Shell