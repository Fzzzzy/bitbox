# bitbox 
* add public/private key encryption algorithm 
* add UDP mode (Use mode=tcp or mode=udp to indicate which mode the server should start with)
* change configuration.properties content
* Handing errors of UDP: packet loss (timeout), inorder

## File system (previous version)
* All communication will be via persistent TCP connections between the peers
* All messages will be in JSON format, one JSON message per line
* Interactions will in the most part be asynchronous request/reply between peers
