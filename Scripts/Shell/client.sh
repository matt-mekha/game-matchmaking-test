cd ../../Java/WorkingDirectory
for i in `seq $1`; do
    java -jar Client.jar $2 &
done
cd ../../Scripts/Batch