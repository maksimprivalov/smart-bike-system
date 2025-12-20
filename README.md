# SmartBike

## IoT Project 2025/26

**Repository:** https://github.com/maksimprivalov/smart-bike-system

---

## 1. Project Description

SmartBike is a Raspberry Pi-based smart bicycle system that provides cyclist safety, navigation, and trip monitoring. The system includes GPS tracking, automatic lighting control, turn signals, and a mobile application for real-time data display.

### 1.1 Project Goals

- Improve cyclist safety (automatic lighting, turn signals)
- Location and route tracking
- Speed and trip statistics monitoring
- User-friendly interface via mobile application

### 1.2 Core Functionality

| Priority   | Feature            | Description                                    |
| ---------- | ------------------ | ---------------------------------------------- |
| Core       | GPS Tracker        | Real-time location tracking                    |
| Core       | Speed Sensor       | Current and average speed measurement          |
| Core       | Turn Signals       | LED turn indicators with button control        |
| Core       | Auto Light         | Automatic headlight activation by light sensor |
| Core       | Mobile Application | Display all data on smartphone                 |
| Additional | Alarm System       | Motion sensor + sound alert                    |
| Additional | Trip History       | Recording and analyzing trip statistics        |
| Additional | Electronic Horn    | Sound signal by button press                   |

---

## 2. Hardware

### 2.1 Main Components

| Component      | Model                 | Qty | Purpose                             |
| -------------- | --------------------- | --- | ----------------------------------- |
| Microcomputer  | Raspberry Pi 4/5      | 1   | Central controller                  |
| GPS Module     | NEO-6M / NEO-7M       | 1   | Coordinates detection               |
| Hall Sensor    | KY-003 / A3144        | 1   | Speed measurement (wheel rotations) |
| Photoresistor  | GL5528 (LDR)          | 1   | Light sensor                        |
| ADC            | ADS1115               | 1   | Analog-to-digital conversion        |
| LED Strip/LEDs | WS2812B / regular LED | 2   | Turn signals (left/right)           |
| LED Headlight  | High-power white 3W   | 1-2 | Front/rear headlight                |
| Buttons        | Tactile buttons       | 3   | Left/right turn, horn*              |
| Buzzer         | Active buzzer 5V      | 1   | Horn*, alarm*                       |
| PIR Sensor     | HC-SR501              | 1   | Alarm* (optional)                   |
| Powerbank      | 5V, 10000+ mAh        | 1   | System power supply                 |

\*— additional components

### 2.2 Raspberry Pi GPIO Connection Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      RASPBERRY PI GPIO                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  3.3V (1) ●────────────────────────────────● (2) 5V             │
│  SDA  (3) ●──→ ADS1115 SDA                 ● (4) 5V ──→ LED lamp│
│  SCL  (5) ●──→ ADS1115 SCL                 ● (6) GND            │
│  GP4  (7) ●──→ Hall Sensor (DATA)          ● (8) TXD ──→ GPS TX │
│  GND  (9) ●                                ● (10) RXD ←── GPS RX│
│  GP17(11) ●──→ LEFT turn button            ● (12) GP18 ──→ PWM light│
│  GP27(13) ●──→ RIGHT turn button           ● (14) GND            │
│  GP22(15) ●──→ HORN button*                ● (16) GP23 ──→ Buzzer*│
│  3.3V(17) ●                                ● (18) GP24 ──→ LED Left│
│  MOSI(19) ●                                ● (20) GND            │
│  MISO(21) ●                                ● (22) GP25 ──→ LED Right│
│  SCLK(23) ●                                ● (24) GP8             │
│  GND (25) ●                                ● (26) GP7             │
│  GP0 (27) ●                                ● (28) GP1             │
│  GP5 (29) ●──→ PIR sensor* (DATA)          ● (30) GND            │
│  GP6 (31) ●                                ● (32) GP12            │
│  GP13(33) ●                                ● (34) GND            │
│  GP19(35) ●                                ● (36) GP16            │
│  GP26(37) ●                                ● (38) GP20            │
│  GND (39) ●                                ● (40) GP21            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

* — additional components
```

### 2.3 Detailed Component Connections

#### 2.3.1 GPS Module (NEO-6M)

| GPS Pin | Raspberry Pi Pin    | Description        |
| ------- | ------------------- | ------------------ |
| VCC     | 3.3V (Pin 1)        | Power              |
| GND     | GND (Pin 6)         | Ground             |
| TX      | RXD GPIO15 (Pin 10) | Data transfer → Pi |
| RX      | TXD GPIO14 (Pin 8)  | Data receive ← Pi  |

#### 2.3.2 Hall Sensor (KY-003)

| Sensor Pin | Raspberry Pi Pin | Description    |
| ---------- | ---------------- | -------------- |
| VCC        | 3.3V (Pin 17)    | Power          |
| GND        | GND (Pin 9)      | Ground         |
| DATA       | GPIO4 (Pin 7)    | Digital signal |

**Operating Principle:** A magnet is attached to the wheel spoke. The sensor triggers with each rotation.

**Speed Calculation Formula:**

```
Speed (km/h) = (Wheel circumference in m) × (Rotations per minute) × 60 / 1000
```

#### 2.3.3 Photoresistor (LDR) + ADS1115 ADC

Raspberry Pi has no analog inputs, so we use an external ADC.

| ADS1115 Pin | Raspberry Pi Pin  | Description  |
| ----------- | ----------------- | ------------ |
| VCC         | 3.3V (Pin 1)      | Power        |
| GND         | GND (Pin 6)       | Ground       |
| SDA         | GPIO2 SDA (Pin 3) | I2C Data     |
| SCL         | GPIO3 SCL (Pin 5) | I2C Clock    |
| A0          | ← LDR divider     | Analog input |

**Voltage Divider Circuit:**

```
3.3V ────┬──── LDR ────┬──── 10kΩ ────┬──── GND
         │             │              │
         │             └───→ A0 (ADS1115)
         │
```

#### 2.3.4 Turn Signals (LED)

**Option 1: Regular LEDs**

| Component       | Raspberry Pi Pin | Description          |
| --------------- | ---------------- | -------------------- |
| Left LED (+)    | GPIO24 (Pin 18)  | Through 220Ω resistor|
| Right LED (+)   | GPIO25 (Pin 22)  | Through 220Ω resistor|
| LED common (−)  | GND              | Ground               |
| Left button     | GPIO17 (Pin 11)  | With internal pull-up|
| Right button    | GPIO27 (Pin 13)  | With internal pull-up|

**Option 2: WS2812B (Addressable LEDs)**

| WS2812B | Raspberry Pi Pin | Description     |
| ------- | ---------------- | --------------- |
| VCC     | 5V (Pin 2)       | Power           |
| GND     | GND              | Ground          |
| DIN     | GPIO18 (Pin 12)  | PWM data signal |

#### 2.3.5 Front Headlight

For high-power LEDs (>20mA) we use a transistor switch:

| Component  | Connection                            |
| ---------- | ------------------------------------- |
| GPIO18     | → 1kΩ Resistor → Transistor base      |
| Collector  | → LED (−)                             |
| Emitter    | → GND                                 |
| LED (+)    | ← 5V through current-limiting resistor|

**Circuit Diagram:**

```
GPIO18 ──── 1kΩ ────┬──── Base (2N2222)
                    │
               Collector ──── LED(−) ──── LED(+) ──── Resistor ──── 5V
                    │
               Emitter ──── GND
```

#### 2.3.6 Additional Components

**Buzzer (Horn/Alarm):**

| Buzzer Pin | Raspberry Pi Pin |
| ---------- | ---------------- |
| (+)        | GPIO23 (Pin 16)  |
| (−)        | GND              |

**PIR Sensor (Alarm):**

| PIR Pin | Raspberry Pi Pin |
| ------- | ---------------- |
| VCC     | 5V (Pin 4)       |
| GND     | GND (Pin 30)     |
| OUT     | GPIO5 (Pin 29)   |

### 2.4 GPIO Summary Table

| GPIO   | Pin | Component      | Type   | Note      |
| ------ | --- | -------------- | ------ | --------- |
| GPIO2  | 3   | ADS1115 SDA    | I2C    |           |
| GPIO3  | 5   | ADS1115 SCL    | I2C    |           |
| GPIO4  | 7   | Hall Sensor    | Input  | Interrupt |
| GPIO14 | 8   | GPS TX         | UART   |           |
| GPIO15 | 10  | GPS RX         | UART   |           |
| GPIO17 | 11  | Left button    | Input  | Pull-up   |
| GPIO18 | 12  | Headlight (PWM)| Output | PWM channel|
| GPIO27 | 13  | Right button   | Input  | Pull-up   |
| GPIO22 | 15  | Horn button*   | Input  | Pull-up   |
| GPIO23 | 16  | Buzzer*        | Output |           |
| GPIO24 | 18  | Left LED       | Output |           |
| GPIO25 | 22  | Right LED      | Output |           |
| GPIO5  | 29  | PIR sensor*    | Input  |           |

---

## 3. Software

### 3.1 Technology Stack

| Component          | Technology                     |
| ------------------ | ------------------------------ |
| Language           | Python 3.11+                   |
| GPIO               | RPi.GPIO / gpiozero            |
| GPS                | pynmea2, pyserial              |
| I2C (ADC)          | adafruit-circuitpython-ads1x15 |
| Mobile Application | Kotlin                         |
| Pi ↔ Phone Link    | Bluetooth / WiFi (Flask)       |
| Database           | SQLite                         |

### 3.2 System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         SMARTBIKE SYSTEM                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐             │
│  │   SENSORS   │    │  RASPBERRY  │    │  ACTUATORS  │             │
│  │             │    │     PI      │    │             │             │
│  │ • GPS       │───→│             │───→│ • LED light │             │
│  │ • Hall      │    │  ┌───────┐  │    │ • Turn sig. │             │
│  │ • LDR       │───→│  │ Main  │  │───→│ • Buzzer*   │             │
│  │ • Buttons   │    │  │ Loop  │  │    │             │             │
│  │ • PIR*      │───→│  └───────┘  │    └─────────────┘             │
│  └─────────────┘    │      │      │                                │
│                     │      ↓      │                                │
│                     │  ┌───────┐  │    ┌─────────────┐             │
│                     │  │ BT/   │  │    │   MOBILE    │             │
│                     │  │ WiFi  │←─┼───→│     APP     │             │
│                     │  └───────┘  │    └─────────────┘             │
│                     │      │      │                                │
│                     │      ↓      │                                │
│                     │  ┌───────┐  │                                │
│                     │  │SQLite │  │                                │
│                     │  └───────┘  │                                │
│                     └─────────────┘                                │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.3 Project Structure (Approximate)

```
smartbike/
├── main.py                 # Entry point
├── config.py               # GPIO pin configuration
├── config.json             # Simulation settings
│
├── sensors/
│   ├── __init__.py
│   ├── gps_sensor.py       # GPS module
│   ├── speed_sensor.py     # Hall sensor
│   ├── light_sensor.py     # Photoresistor (LDR)
│   ├── button_handler.py   # Buttons
│   └── pir_sensor.py       # PIR* (optional)
│
├── actuators/
│   ├── __init__.py
│   ├── headlight.py        # Headlight
│   ├── turn_signals.py     # Turn signals
│   └── buzzer.py           # Buzzer*
│
├── communication/
│   ├── __init__.py
│   ├── bluetooth_server.py # Bluetooth
│   └── wifi_api.py         # REST API
│
├── data/
│   ├── __init__.py
│   ├── database.py         # SQLite
│   └── trip_recorder.py    # Trip recording*
│
├── simulation/
│   ├── __init__.py
│   ├── gps_sim.py
│   ├── speed_sim.py
│   └── light_sim.py
│
└── utils/
    ├── __init__.py
    └── logger.py
```

### 3.4 Configuration File (config.json) (Approximate)

```json
{
	"simulation": {
		"gps": false,
		"speed_sensor": false,
		"light_sensor": false,
		"buttons": false,
		"pir": true
	},
	"gpio": {
		"hall_sensor": 4,
		"button_left": 17,
		"button_right": 27,
		"button_horn": 22,
		"led_left": 24,
		"led_right": 25,
		"headlight": 18,
		"buzzer": 23,
		"pir": 5
	},
	"wheel": {
		"circumference_cm": 207
	},
	"light": {
		"threshold_on": 300,
		"threshold_off": 500
	},
	"gps": {
		"port": "/dev/ttyS0",
		"baudrate": 9600
	}
}
```

---

## 4. 3D-Printed Enclosures

All enclosures will be designed in Fusion 360 and 3D printed.

### 4.1 Enclosure List (Tentative)

| #   | Enclosure          | Contents                 | Location              | Size (approx.)    |
| --- | ------------------ | ------------------------ | --------------------- | ----------------- |
| 1   | Main Unit          | Raspberry Pi + Powerbank | Under seat / on frame | 150×100×50 mm     |
| 2   | GPS Module         | NEO-6M + antenna         | On handlebar          | 50×50×20 mm       |
| 3   | Control Panel      | 3 buttons                | On handlebar          | 80×40×25 mm       |
| 4   | Light Sensor       | LDR + ADS1115            | On handlebar (up)     | 40×30×20 mm       |
| 5   | Left Turn Signal   | LED module               | Left handlebar edge   | 60×30×15 mm       |
| 6   | Right Turn Signal  | LED module               | Right handlebar edge  | 60×30×15 mm       |
| 7   | Front Headlight    | 3W LED + heatsink        | On handlebar          | 50×50×40 mm       |
| 8   | Speed Sensor       | Hall sensor              | On fork               | 30×20×15 mm       |
| 9   | Magnet Holder      | Neodymium magnet         | On spoke              | 15×10×10 mm       |

### 4.2 Enclosure Requirements

| Requirement  | Specification                                        |
| ------------ | ---------------------------------------------------- |
| Material     | PETG or ASA (UV and moisture resistant)              |
| Protection   | IP54 minimum (splash protection)                     |
| Mounting     | Universal clamps 22-32 mm or integrated in enclosure |
| Access       | Snap-fit covers for maintenance                      |
| Ventilation  | Vents for main unit (Pi cooling)                     |
| Cables       | PG7 cable glands for sealed entry                    |

### 4.3 Model Files

Location: `/hardware/3d-models/`

```
3d-models/
├── main-enclosure/
│   ├── main-enclosure.stl
│   ├── main-enclosure.step
│   └── main-enclosure.f3d
├── gps-mount/
├── control-panel/
├── light-sensor-housing/
├── turn-signal-left/
├── turn-signal-right/
├── headlight-housing/
├── hall-sensor-mount/
└── magnet-holder/
```

---

## 5. Mobile Application

### 5.1 Screens

| Screen       | Functionality                    |
| ------------ | -------------------------------- |
| Dashboard    | Speed, GPS, system status        |
| Map          | Current position, route track    |
| Statistics*  | Trip history, graphs             |
| Settings     | Calibration, thresholds, sounds  |

### 5.2 Data Exchange Protocol

**Format:** JSON via Bluetooth SPP or WiFi REST API

```json
{
	"timestamp": 1703001234,
	"gps": {
		"lat": 45.2671,
		"lon": 19.8335,
		"satellites": 8
	},
	"speed": {
		"current": 18.2,
		"average": 14.5,
		"max": 32.1
	},
	"trip": {
		"distance_km": 5.23,
		"duration_sec": 1234
	},
	"system": {
		"headlight": true,
		"turn_signal": "none",
		"light_level": 234
	}
}
```

---

## 6. Sample Interface

```
╔══════════════════════════════════════════════════════════════╗
║                    SMARTBIKE CONSOLE v1.0                    ║
╠══════════════════════════════════════════════════════════════╣
║  GPS: 45.2671°N, 19.8335°E | Satellites: 8                   ║
║  Speed: 18.2 km/h | Avg: 14.5 km/h | Max: 32.1 km/h          ║
║  Distance: 5.23 km | Time: 00:20:34                          ║
║  Light level: 234 | Headlight: ON (auto)                     ║
║  Turn signal: NONE | Alarm: DISARMED                         ║
╠══════════════════════════════════════════════════════════════╣
║  [L] Left   [R] Right   [H] Headlight   [Q] Quit             ║
║  [K] Horn*  [A] Alarm*  [S] Statistics                       ║
╚══════════════════════════════════════════════════════════════╝
```

## 7. Control Commands

| Key   | Action                              |
| ----- | ----------------------------------- |
| `L`   | Toggle left turn signal (blinking)  |
| `R`   | Toggle right turn signal (blinking) |
| `H`   | Switch headlight mode (auto/on/off) |
| `K`*  | Sound the horn                      |
| `A`*  | Toggle alarm                        |
| `S`   | Show trip statistics                |
| `Q`   | Exit program                        |

\*— additional functions

---

## 9. Team

| Role      | Name            | GitHub                            |
| --------- | --------------- | --------------------------------- |
| Developer | Avanesov Roman  | https://github.com/TortAlpha      |
| Developer | Privalov Maksim | https://github.com/maksimprivalov |