# GPIO
GPIO = {
    "hall_sensor": 4,
    "button_left": 17,
    "button_right": 27,
    "button_horn": 22,
    "led_left": 24,
    "led_right": 25,
    "headlight": 18,
    "buzzer": 23,
    "pir": 5
}

WHEEL = {
    "circumference_m": 2.07  # 207 santi meters in meter
}

# Light sensor 
LIGHT = {
    "threshold_on": 300,
    "threshold_off": 500
}

# GPS
GPS = {
    "port": "/dev/ttyAMA0",
    "baudrate": 9600
}

SIMULATION = {
    "gps": False,
    "speed_sensor": False,
    "light_sensor": False,
    "buttons": False,
    "pir": True
}

UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee"