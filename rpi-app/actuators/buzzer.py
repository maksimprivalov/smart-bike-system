import time
import threading
from utils.logger import get_logger
from config import GPIO

log = get_logger("Buzzer")

class BuzzerActuator:
    def __init__(self):
        self._buzzer = None
        self._setup()

    def _setup(self):
        try:
            from gpiozero import Buzzer
            self._buzzer = Buzzer(GPIO["buzzer"])
            log.info(" OK! Buzzer initialized.")
        except Exception as e:
            log.error(f"ERROR! Buzzer: {e}")

    def beep(self, times: int = 1, duration: float = 0.2):
        def _do():
            for _ in range(times):
                if self._buzzer:
                    self._buzzer.on()
                time.sleep(duration)
                if self._buzzer:
                    self._buzzer.off()
                time.sleep(0.1)
        threading.Thread(target=_do, daemon=True).start()

    def horn(self):
        self.beep(times=1, duration=0.5)
        log.info("Horn!")

    def alarm(self):
        self.beep(times=3, duration=0.1)
        log.info("Alarm!")
