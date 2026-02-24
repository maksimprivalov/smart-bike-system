from utils.logger import get_logger
from config import GPIO

log = get_logger("Headlight")

class Headlight:
    MODE_AUTO = "auto"
    MODE_ON   = "on"
    MODE_OFF  = "off"

    def __init__(self):
        self.mode = self.MODE_AUTO
        self.state = False
        self._led = None
        self._setup()

    def _setup(self):
        try:
            from gpiozero import LED
            self._led = LED(GPIO["headlight"])
            log.info("OK! Headlight initialized.")
        except Exception as e:
            log.error(f"ERROR! Headlight : {e}")

    def update(self, is_dark: bool):
        if self.mode == self.MODE_AUTO:
            if is_dark and not self.state:
                self.turn_on()
            elif not is_dark and self.state:
                self.turn_off()
        elif self.mode == self.MODE_ON:
            if not self.state:
                self.turn_on()
        elif self.mode == self.MODE_OFF:
            if self.state:
                self.turn_off()

    def turn_on(self):
        self.state = True
        if self._led:
            self._led.on()
        log.info("Headlight turned on.")

    def turn_off(self):
        self.state = False
        if self._led:
            self._led.off()
        log.info("Headlight turned on.")

    def toggle_mode(self):
        modes = [self.MODE_AUTO, self.MODE_ON, self.MODE_OFF]
        idx = modes.index(self.mode)
        self.mode = modes[(idx + 1) % len(modes)]
        log.info(f"Headlight mode: {self.mode}")

    def get_data(self) -> dict:
        return {
            "state": self.state,
            "mode": self.mode
        }
