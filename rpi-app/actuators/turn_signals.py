import threading
import time
from utils.logger import get_logger
from config import GPIO

log = get_logger("TurnSignals")

class TurnSignals:
    NONE  = "none"
    LEFT  = "left"
    RIGHT = "right"

    def __init__(self):
        self.state = self.NONE
        self._led_left = None
        self._led_right = None
        self._blink_thread = None
        self._blinking = False
        self._setup()

    def _setup(self):
        try:
            from gpiozero import LED
            self._led_left = LED(GPIO["led_left"])
            self._led_right = LED(GPIO["led_right"])
            log.info("OK! Turn signals initialized.")
        except Exception as e:
            log.error(f"ERROR! Turn signals : {e}")

    def toggle_left(self):
        if self.state == self.LEFT:
            self.off()
        else:
            self._start_blink(self.LEFT)

    def toggle_right(self):
        if self.state == self.RIGHT:
            self.off()
        else:
            self._start_blink(self.RIGHT)

    def off(self):
        self._blinking = False
        self.state = self.NONE
        if self._led_left:
            self._led_left.off()
        if self._led_right:
            self._led_right.off()
        log.info("Turn signals off")

    def _start_blink(self, direction: str):
        self.off()
        self.state = direction
        self._blinking = True
        self._blink_thread = threading.Thread(
            target=self._blink_loop,
            args=(direction,),
            daemon=True
        )
        self._blink_thread.start()
        log.info(f"Turn signal : {direction}")

    def _blink_loop(self, direction: str):
        led = self._led_left if direction == self.LEFT else self._led_right
        while self._blinking:
            if led:
                led.on()
            time.sleep(0.5)
            if led:
                led.off()
            time.sleep(0.5)

    def get_data(self) -> dict:
        return {"state": self.state}
