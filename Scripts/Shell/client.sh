cd ../../Java/WorkingDirectory
for i in `seq $1`; do
    java -jar Client.jar &
done
cd ../../Scripts/Batch