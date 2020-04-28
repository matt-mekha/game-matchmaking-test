cd ../workingDirectory
del *.txt /Q
for /l %%x in (1, 1, %1) do start "" java -jar client.jar %2
cd ../scripts