import socket
server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server.bind(("192.168.141.24",5555))
server.listen()

client,address = server.accept()

print(f"Connected to client {address[0]}")
file_bytes = b""
done = False
check = client.recv(1024)
if check.decode()=="sending image":
    while not done:
        data = client.recv(1024)
        if (file_bytes[-5:] == b"<END>" or data[-5:] == b"<END>"):
            done = True
        else:
            file_bytes += data

    with open("Recieved.jpg","wb") as f:
        f.write(file_bytes)

    with open("file1.jpg","rb") as f:
        file_bytes = f.read()
        size = str(len(file_bytes))
    print("file size is:",size)

    client.send("t-shirt".encode())
    print(client.recv(1024))
    client.send("Jack and Jones".encode())
    print(client.recv(1024))
    client.send(size.encode())
    print(client.recv(1024))
    client.sendall(file_bytes)
    client.sendall("<END>".encode())
    print(client.recv(1024))
    # print((client.recv(1024)).decode()) #cross verify

    # client.sendall("This is the image from server".encode())#4
else:
    client.close()