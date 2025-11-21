import socket
import pygame
from ctypes import Structure, c_int8, c_uint8

# === Network settings ===
TARGET_IP = "10.244.245.249"   # your ESP8266 IP
TARGET_PORT = 4210
SPEED = 255
FLAGS = 0


# === Define the ControlData struct (C-compatible) ===
class ControlData(Structure):
    _fields_ = [
        ("forward", c_int8),
        ("turn", c_int8),
        ("speed", c_uint8),
        ("flags", c_uint8),
        ("checksum", c_uint8),
    ]


def make_packet(forward: c_int8, turn: c_int8, speed: c_uint8, flags: c_uint8):
    packet = ControlData()
    packet.forward = forward
    packet.turn = turn
    packet.speed = speed
    packet.flags = flags
    packet.checksum = (forward + turn + speed + flags) & 0xFF
    return (bytes(packet), packet)


# === UDP Socket setup ===
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.setblocking(False)

# === Pygame setup ===
pygame.init()
pygame.key.set_repeat(50, 50)
screen = pygame.display.set_mode((480, 120))
pygame.display.set_caption(f"UDP Bot Control -> {TARGET_IP}:{TARGET_PORT}")
font = pygame.font.Font(None, 24)
clock = pygame.time.Clock()

running = True
while running:
    for event in pygame.event.get():
        if event.type == pygame.QUIT or (event.type == pygame.KEYDOWN and event.key == pygame.K_ESCAPE):
            running = False

    keys = pygame.key.get_pressed()
    forward: c_int8 = 0
    turn: c_int8 = 0

    if keys[pygame.K_w]:
        forward = 127
    elif keys[pygame.K_s]:
        forward = -127

    if keys[pygame.K_a]:
        turn = -127
    elif keys[pygame.K_d]:
        turn = 127

    if (forward != 0 or turn != 0):
        # Build packet and send
        packet = make_packet(forward, turn, SPEED, FLAGS)
        try:
            print(f"{forward} {turn}")
            sock.sendto(packet[0], (TARGET_IP, TARGET_PORT))
        except Exception as e:
            print("Send failed:", e)

    # UI
    screen.fill((30, 30, 30))
    status = f"F:{forward:>4}  T:{turn:>4}  Speed:{SPEED}"
    screen.blit(font.render("Press W/A/S/D to move, ESC to quit.", True, (230,230,230)), (10, 10))
    screen.blit(font.render(status, True, (200,200,120)), (10, 40))
    pygame.display.flip()
    clock.tick(240)

pygame.quit()
sock.close()
