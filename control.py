#!/usr/bin/env python3
# Minimal: open a pygame window and send a UDP packet on every keydown.
# Set TARGET_IP and TARGET_PORT below.

import socket
import sys

try:
    import pygame
except ImportError:
    print("Install pygame: pip install pygame")
    sys.exit(1)

TARGET_IP = "10.244.245.249"   # <- change to your target IP
TARGET_PORT = 4210            # <- change to your target port

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.setblocking(False)
sock.sendto(b"H", (TARGET_IP, TARGET_PORT))

pygame.init()
pygame.key.set_repeat(50, 50)  # delay 50ms, interval 50ms
screen = pygame.display.set_mode((480, 120))
pygame.display.set_caption(f"UDP Key Sender -> {TARGET_IP}:{TARGET_PORT}")
font = pygame.font.Font(None, 24)
clock = pygame.time.Clock()
running = True

while running:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False
        elif event.type == pygame.KEYDOWN and event.key == pygame.K_ESCAPE:
            running = False

    keys = pygame.key.get_pressed()

    forward = 0
    turn = 0

    if keys[pygame.K_w]:
        forward = 127
    elif keys[pygame.K_s]:
        forward = -127

    if keys[pygame.K_a]:
        turn = -127
    elif keys[pygame.K_d]:
        turn = 127

    payload = b""
    if keys[pygame.K_w]:
        payload = b'f'
    elif keys[pygame.K_s]:
        payload = b'b'
    elif keys[pygame.K_a]:
        payload = b'l'
    elif keys[pygame.K_d]:
        payload = b'r'

    if payload:
        try:
            sock.sendto(payload, (TARGET_IP, TARGET_PORT))
            print("Sent:", payload)
        except Exception as e:
            print("Send failed:", e)

    screen.fill((30, 30, 30))
    screen.blit(font.render("Focus this window and press keys to send UDP.", True, (230,230,230)), (10, 10))
    screen.blit(font.render("Press ESC or close window to quit.", True, (200,200,120)), (10, 40))
    pygame.display.flip()
    clock.tick(60)

pygame.quit()
sock.close()
