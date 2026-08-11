# Variables
FQBN = esp8266:esp8266:nodemcuv2
PORT = /dev/ttyUSB0
BOARD_URL = http://arduino.esp8266.com/stable/package_esp8266com_index.json
BAUD = 115200

# Targets
.PHONY: all compile upload tty clean

setup:
	arduino-cli config set board_manager.additional_urls $(BOARD_URL)
	arduino-cli core update-index
	arduino-cli core install esp8266:esp8266

all: compile

compile:
	arduino-cli compile --fqbn $(FQBN) .

upload:
	arduino-cli upload -p $(PORT) --fqbn $(FQBN) --additional-urls $(BOARD_URL) .

tty:
	arduino-cli monitor -p $(PORT) -b $(FQBN) -c baudrate=$(BAUD)
