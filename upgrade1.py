import threading
import socket
import support

all_customer = []
all_customer_address = []
all_sellers = []
all_sellers_address = []

def create():
    global server
    global st 
    server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    server.bind(("192.168.1.11",5555))
    server.listen()
    
def accepting():
    while True:
        try:
            conn , address = server.accept()
            server.setblocking(1)
            check = conn.recv(1024)
            if check == b"search_image":
                all_customer.append(conn)
                all_customer_address.append(address)
                print("Customer Connection has been established "+address[0])
                threading.Thread(target=serve_customer).start()

            if check == b"upload_product":
                all_sellers.append(conn)
                all_sellers_address.append(address)
                print("Seller Connection has been established "+address[0])
                threading.Thread(target=seller_add_product()).start()
            
        except socket.error as msg:
            print(msg)

def seller_add_product():
        client = all_sellers[len(all_sellers)-1]
        file_bytes = b""
        done = False
        client.send("1".encode())
        product_id = (client.recv(1024)).decode()
        client.send("1".encode())
        while not done:
            data = client.recv(1024)
            if (file_bytes[-5:] == b"<END>"):
                done = True
            else:
                file_bytes += data
        print("\n",len(file_bytes),end="\n")
        support.save_image(product_id,file_bytes)
        client.close()

def serve_customer():
    client = all_customer[len(all_customer)-1]
    file_bytes = b""
    done = False
    client.send("1".encode())
    while not done:
        if (file_bytes[-5:] == b"<END>"):
            done = True
        else:
            data = client.recv(1024)
            file_bytes += data

    with open("receive\\Received.jpg","wb") as f:
        f.write(file_bytes)

    uri = support.find_similar_image("receive\\Received.jpg")

    client.send(uri[0].encode())
    print(client.recv(1024))

    client.send(uri[1].encode())
    print(client.recv(1024))

    client.send(uri[2].encode())
    print(client.recv(1024))

    client.close()
    

create()
accepting()