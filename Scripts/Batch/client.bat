cd ../../Java/WorkingDirectory
for /l %%N in (1 1 %1) do (
    start "" java -jar Client.jar
)
cd ../../Scripts/Batch