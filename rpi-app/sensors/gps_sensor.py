import threading
import time
import serial
import pynmea2
from utils.logger import get_logger
from config import GPS, SIMULATION

log = get_logger("GPS")

class GPSSensor:
    def __init__(self):
        self.lat = None
        self.lon = None
        self.satellites = 0
        self.fix = False
        self._running = False
        self._thread = None

    def start(self):
        self._running = True
        if SIMULATION["gps"]:
            self._thread = threading.Thread(target=self._simulate, daemon=True)
        else:
            self._thread = threading.Thread(target=self._read, daemon=True)
        self._thread.start()
        log.info("OK! GPS started.")

    def stop(self):
        self._running = False
        log.info("GPS stopped.")

    def _read(self):
        try:
            ser = serial.Serial(GPS["port"], baudrate=GPS["baudrate"], timeout=2)
            while self._running:
                line = ser.readline().decode('ascii', errors='replace').strip()
                if line.startswith('$GPRMC') or line.startswith('$GNRMC'):
                    try:
                        msg = pynmea2.parse(line)
                        if msg.status == 'A':
                            self.lat = msg.latitude
                            self.lon = msg.longitude
                            self.fix = True
                    except Exception:
                        pass
                elif line.startswith('$GPGSV') or line.startswith('$GNGSV'):
                    try:
                        msg = pynmea2.parse(line)
                        self.satellites = int(msg.num_sv_in_view or 0)
                    except Exception:
                        pass
        except Exception as e:
            log.error(f"Ошибка GPS: {e}")

    def _simulate(self):
        # Simulate movements in NoviSad
        self.lat = 45.2671
        self.lon = 19.8335
        self.satellites = 8
        self.fix = True
        while self._running:
            self.lat += 0.0001
            self.lon += 0.0001
            time.sleep(1)

    def get_data(self) -> dict:
        return {
            "lat": round(self.lat, 6) if self.lat else None,
            "lon": round(self.lon, 6) if self.lon else None,
            "satellites": self.satellites,
            "fix": self.fix
        }
