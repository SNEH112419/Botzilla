import socket

# Bind to all interfaces on port 4210
UDP_IP = "0.0.0.0"
UDP_PORT = 4210

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((UDP_IP, UDP_PORT))

print(f"Listening for UDP packets on port {UDP_PORT}...")

while True:
    data, addr = sock.recvfrom(1024)  # Buffer size 1024 bytes
    print(f"[{addr[0]}:{addr[1]}] -> {data}")
