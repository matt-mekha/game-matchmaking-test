gradle clean
gradle desktop:dist
cd desktop/build/libs
mv desktop.jar Client.jar
cd ../../..
gradle desktop:copy
exit