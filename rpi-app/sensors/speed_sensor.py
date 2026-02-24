import threading
import time
from utils.logger import get_logger
from config import GPIO, WHEEL, SIMULATION

log = get_logger("Speed")

class SpeedSensor:
    def __init__(self):
        self.current_speed = 0.0
        self.avg_speed = 0.0
        self.max_speed = 0.0
        self.total_rotations = 0
        self._rotation_times = []
        self._running = False
        self._thread = None
        self._circumference = WHEEL["circumference_m"]

    def start(self):
        self._running = True
        if SIMULATION["speed_sensor"]:
            self._thread = threading.Thread(target=self._simulate, daemon=True)
            self._thread.start()
        else:
            self._setup_gpio()
        log.info("OK! Speed sensor started.")

    def stop(self):
        self._running = False
        try:
            from gpiozero import Button
            # gpiozero сам чистит при завершении
        except Exception:
            pass
        log.info("Speed sensor stopped.")

    def _setup_gpio(self):
        try:
            from gpiozero import Button
            sensor = Button(GPIO["hall_sensor"], pull_up=True)
            sensor.when_pressed = self._on_rotation
            # Hold here the pointer to save it
            self._sensor = sensor
            # thread for speed calculation
            self._thread = threading.Thread(target=self._calc_loop, daemon=True)
            self._thread.start()
        except Exception as e:
            log.error(f"ERROR! GPIO: {e}")

    def _on_rotation(self):
        now = time.time()
        self._rotation_times.append(now)
        self.total_rotations += 1
        # Keep only 5 last calls
        if len(self._rotation_times) > 5:
            self._rotation_times.pop(0)
        self._calculate_speed()

    def _calculate_speed(self):
        if len(self._rotation_times) < 2:
            return
        # avg time between calls
        intervals = []
        for i in range(1, len(self._rotation_times)):
            intervals.append(self._rotation_times[i] - self._rotation_times[i-1])
        avg_interval = sum(intervals) / len(intervals)
        if avg_interval > 0:
            rps = 1.0 / avg_interval  # rotations per sec
            speed = self._circumference * rps * 3.6  # km per hour
            self.current_speed = round(speed, 1)
            if speed > self.max_speed:
                self.max_speed = round(speed, 1)

    def _calc_loop(self):
        """Set speed to 0 if we stay for more than 3 sec"""
        while self._running:
            time.sleep(1)
            if self._rotation_times:
                if time.time() - self._rotation_times[-1] > 3:
                    self.current_speed = 0.0

    def _simulate(self):
        import math
        t = 0
        while self._running:
            # Симуляция езды: разгон → крейсер → торможение
            self.current_speed = round(15 + 10 * math.sin(t / 10), 1)
            if self.current_speed > self.max_speed:
                self.max_speed = self.current_speed
            t += 1
            time.sleep(1)

    def get_data(self) -> dict:
        return {
            "current": self.current_speed,
            "max": self.max_speed,
        }
