#include <cstdint>

typedef struct ControlData {
    int8_t forward;
    int8_t turn;
    uint8_t speed;
    uint8_t flags;
    uint8_t checksum;
} ControlData;

typedef union ControlPacket {
    ControlData data;
    char buffer[5];
} ControlPacket;

