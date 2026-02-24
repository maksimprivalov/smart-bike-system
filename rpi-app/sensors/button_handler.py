import threading
from utils.logger import get_logger
from config import GPIO, SIMULATION

log = get_logger("Buttons")

class ButtonHandler:
    def __init__(self):
        self.on_left = None    # callback
        self.on_right = None   # callback
        self.on_horn = None    # callback
        self._running = False

    def start(self):
        self._running = True
        if SIMULATION["buttons"]:
            log.info("Buttons in simylation mode (keyboard mode)")
            t = threading.Thread(target=self._simulate, daemon=True)
            t.start()
        else:
            self._setup_gpio()
        log.info("Buttons started")

    def stop(self):
        self._running = False

    def _setup_gpio(self):
        try:
            from gpiozero import Button

            self._btn_left = Button(GPIO["button_left"], pull_up=True)
            self._btn_right = Button(GPIO["button_right"], pull_up=True)
            self._btn_horn = Button(GPIO["button_horn"], pull_up=True)

            self._btn_left.when_pressed = self._on_left
            self._btn_right.when_pressed = self._on_right
            self._btn_horn.when_pressed = self._on_horn

        except Exception as e:
            log.error(f"ERROR! GPIO buttons : {e}")

    def _on_left(self):
        log.info("Left button pressed.")
        if self.on_left:
            self.on_left()

    def _on_right(self):
        log.info("Right button pressed.")
        if self.on_right:
            self.on_right()

    def _on_horn(self):
        log.info("Horn button pressed.")
        if self.on_horn:
            self.on_horn()

    def _simulate(self):
        """Simulation with keyboard: L, R, H"""
        import sys
        import select
        import tty
        import termios

        old_settings = termios.tcgetattr(sys.stdin)
        try:
            tty.setcbreak(sys.stdin.fileno())
            while self._running:
                if select.select([sys.stdin], [], [], 0.1)[0]:
                    key = sys.stdin.read(1).lower()
                    if key == 'l':
                        self._on_left()
                    elif key == 'r':
                        self._on_right()
                    elif key == 'h':
                        self._on_horn()
        finally:
            termios.tcsetattr(sys.stdin, termios.TCSADRAIN, old_settings)
