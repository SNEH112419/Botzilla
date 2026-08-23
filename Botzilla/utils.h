#pragma once
#include <cstdint>

#define STR_HELPER(x) #x
#define STR(x) STR_HELPER(x)

typedef struct {
    int8_t forward;
    int8_t turn;
    uint8_t speed;
    uint8_t flags;
    // 1st bit for brake
    uint8_t elevation;
    uint8_t grip;
    uint8_t checksum;
} __attribute__((__packed__)) ControlData;

typedef union {
    ControlData data;
    uint8_t buffer[sizeof(ControlData)];
} ControlPacket;

