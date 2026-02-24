import threading
import time
from utils.logger import get_logger
from config import LIGHT, SIMULATION

log = get_logger("Light")

class LightSensor:
    def __init__(self):
        self.value = 1000
        self.voltage = 3.3
        self._running = False
        self._thread = None

    def start(self):
        self._running = True
        if SIMULATION["light_sensor"]:
            self._thread = threading.Thread(target=self._simulate, daemon=True)
        else:
            self._thread = threading.Thread(target=self._read, daemon=True)
        self._thread.start()
        log.info("OK! Light sensor started.")

    def stop(self):
        self._running = False
        log.info("Light sesnor stopped.")

    def _read(self):
        try:
            import board
            import busio
            import adafruit_ads1x15.ads1115 as ADS
            from adafruit_ads1x15.analog_in import AnalogIn

            i2c = busio.I2C(board.SCL, board.SDA)
            ads = ADS.ADS1115(i2c)
            chan = AnalogIn(ads, 0)

            while self._running:
                self.value = chan.value
                self.voltage = round(chan.voltage, 3)
                time.sleep(0.5)
        except Exception as e:
            log.error(f"ERROR! ADS1115: {e}")

    def _simulate(self):
        import math
        t = 0
        while self._running:
            # Simulation day/night changing
            self.value = int(500 + 400 * math.sin(t / 20))
            t += 1
            time.sleep(1)

    def is_dark(self) -> bool:
        return self.value < LIGHT["threshold_on"]

    def get_data(self) -> dict:
        return {
            "value": self.value,
            "voltage": self.voltage,
            "is_dark": self.is_dark()
        }
