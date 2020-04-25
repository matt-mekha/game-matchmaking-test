cd ../../Java
if not exist WorkingDirectory mkdir WorkingDirectory
del WorkingDirectory/*.*
cd Client
start "" build
cd ../Servers/MatchmakingServer
start "" build
cd ../GameServer
start "" build
cd ../../../Scripts/Batch