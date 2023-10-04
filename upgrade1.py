import threading
import socket

all_customer = []
all_customer_address = []
all_sellers = []
all_sellers_address = []

def create():
    global server
    server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    server.bind(("192.168.83.24",5555))
    server.listen()
    
def accepting():
    while True:
        try:
            conn , address = server.accept()
            server.setblocking(1)
            check = conn.recv(1024)
            if check == b"sending image":
                all_customer.append(conn)
                all_customer_address.append(address)
                print("Customer Connection has been established "+address[0])
                threading.Thread(target=serve_customer).start()

            if check == b"seller_id":
                all_sellers.append(conn)
                all_sellers_address.append(address)
            
        except socket.error as msg:
            print(msg)
def serve_customer(no=0):
    client = all_customer[len(all_customer)-1]
    file_bytes = b""
    done = False
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

create()
accepting()