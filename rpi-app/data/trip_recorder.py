import time
from utils.logger import get_logger

log = get_logger("Trip")

class TripRecorder:
    def __init__(self):
        self.distance_km = 0.0
        self.start_time = None
        self.duration_sec = 0
        self._speed_samples = []
        self._running = False

    def start(self):
        self.start_time = time.time()
        self._running = True
        log.info("Trip started!")

    def stop(self):
        self._running = False
        self.duration_sec = int(time.time() - self.start_time)
        log.info(f"Trip ended, results : {self.distance_km:.2f} km, {self.duration_sec} sec")

    def update(self, speed_kmh: float, interval_sec: float = 1.0):
        if not self._running:
            return
        self.distance_km += (speed_kmh / 3600) * interval_sec
        self.distance_km = round(self.distance_km, 3)
        self.duration_sec = int(time.time() - self.start_time)
        if speed_kmh > 0:
            self._speed_samples.append(speed_kmh)

    @property
    def avg_speed(self) -> float:
        if not self._speed_samples:
            return 0.0
        return round(sum(self._speed_samples) / len(self._speed_samples), 1)

    def get_data(self) -> dict:
        return {
            "distance_km": self.distance_km,
            "duration_sec": self.duration_sec,
            "avg_speed": self.avg_speed
        }
