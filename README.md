## How to run it yourself

### Prerequisites
- Java Runtime Environment
- Gradle

### Windows

#### Building the JARs

Open Command Prompt in the `Scripts/Batch` directory (a quick way to do this is to type "cmd" into the address bar of File Explorer and press enter).
Next, run the `build` command and wait for the three Gradle builds to complete.

#### Running the matchmaking server

If you want to run the matchmaking server on your device, run the following command:
```
run <number of clients>
```
The `<number of clients>` parameter can be left blank if you wish to solely run the matchmaking server.

#### Running the clients

Now, to run some clients, run the following command:
```
client <number of clients> <matchmaking server IPv6 address>
```
The second parameter is optional.
If left blank, the clients will assume the matchmaking server is running on the same device at `127.0.0.1`.
If the matchmaking server is on a different device, visit https://www.whatismyip.com/ on the server's device to find its IPv6 address.
Keep in mind that home network firewalls might block these requests, so it may be necessary to configure a pinhole for port 3000 on the host device.

### Mac or Linux

#### Building the JARs

Open the terminal and navigate to the `Scripts/Shell` directory.
Next, run the following command to build and wait for the three Gradle builds to complete.
```
./build.sh
```

#### Running the matchmaking server

If you want to run the matchmaking server on your device, run the following command:
```
./run.sh <number of clients>
```
The `<number of clients>` parameter can be left blank if you wish to solely run the matchmaking server.

#### Running the clients

Now, to run some clients, run the following command:
```
./client.sh <number of clients> <matchmaking server IPv6 address>
```
The second parameter is optional.
If left blank, the clients will assume the matchmaking server is running on the same device at `127.0.0.1`.
If the matchmaking server is on a different device, visit https://www.whatismyip.com/ on the server's device to find its IPv6 address.
Keep in mind that home network firewalls might block these requests, so it may be necessary to configure a pinhole for port 3000 on the host device.
