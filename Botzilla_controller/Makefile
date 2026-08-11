# Variables
FQBN = esp32:esp32:esp32
PORT = /dev/ttyUSB0
BOARD_URL = https://espressif.github.io/arduino-esp32/package_esp32_index.json
BAUD = 115200

# Targets
.PHONY: all compile upload tty clean

setup:
	arduino-cli config set board_manager.additional_urls $(BOARD_URL)
	arduino-cli core update-index
	arduino-cli core install esp32:esp32

all: compile

compile:
	arduino-cli compile --fqbn $(FQBN) .

upload:
	arduino-cli upload -p $(PORT) --fqbn $(FQBN) --additional-urls $(BOARD_URL) .

tty:
	arduino-cli monitor -p $(PORT) -b $(FQBN) -c baudrate=$(BAUD)
