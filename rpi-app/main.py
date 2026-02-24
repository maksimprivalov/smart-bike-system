import bluetooth
import json
import time
import signal
import sys
import threading

from sensors.gps_sensor import GPSSensor
from sensors.speed_sensor import SpeedSensor
from sensors.light_sensor import LightSensor
from sensors.button_handler import ButtonHandler
from actuators.headlight import Headlight
from actuators.turn_signals import TurnSignals
from actuators.buzzer import BuzzerActuator
from data.trip_recorder import TripRecorder
from utils.logger import get_logger
from config import UUID

log = get_logger("Main")

class SmartBike:
    def __init__(self):
        log.info("=== SmartBike starting... ===")

        self.gps = GPSSensor()
        self.speed = SpeedSensor()
        self.light = LightSensor()
        self.buttons = ButtonHandler()

        self.headlight = Headlight()
        self.turn = TurnSignals()
        self.buzzer = BuzzerActuator()

        self.trip = TripRecorder()

        self._running    = False
        self._client_sock = None

    def start(self):
        self.gps.start()
        self.speed.start()
        self.light.start()
        self.buttons.start()

        # setting callbacks
        self.buttons.on_left  = self.turn.toggle_left
        self.buttons.on_right = self.turn.toggle_right
        self.buttons.on_horn  = self.buzzer.horn

        self.trip.start()

        self._running = True
        self.buzzer.beep(times=2, duration=0.1)

        self._bluetooth_server()

    def stop(self):
        log.info("Stopping...")
        self._running = False
        self.trip.stop()
        self.turn.off()
        self.headlight.turn_off()
        if self._client_sock:
            try:
                self._client_sock.close()
            except Exception:
                pass
        log.info("SmartBike stopped.")

    def _build_payload(self) -> dict:
        gps_data   = self.gps.get_data()
        speed_data = self.speed.get_data()
        light_data = self.light.get_data()
        trip_data  = self.trip.get_data()
        head_data  = self.headlight.get_data()
        turn_data  = self.turn.get_data()

        return {
            "timestamp": int(time.time()),
            "gps": {
                "lat":        gps_data["lat"],
                "lon":        gps_data["lon"],
                "satellites": gps_data["satellites"],
                "fix":        gps_data["fix"]
            },
            "speed": {
                "current": speed_data["current"],
                "average": trip_data["avg_speed"],
                "max":     speed_data["max"]
            },
            "trip": {
                "distance_km":  trip_data["distance_km"],
                "duration_sec": trip_data["duration_sec"]
            },
            "system": {
                "headlight":      head_data["state"],
                "headlight_mode": head_data["mode"],
                "turn_signal":    turn_data["state"],
                "light_level":    light_data["value"]
            }
        }

    def _handle_command(self, data: str):
        try:
            cmd = json.loads(data.strip())
            action = cmd.get("action")

            if action == "turn_left":
                self.turn.toggle_left()
            elif action == "turn_right":
                self.turn.toggle_right()
            elif action == "turn_off":
                self.turn.off()
            elif action == "horn":
                self.buzzer.horn()
            elif action == "headlight":
                mode = cmd.get("mode", "auto")
                self.headlight.mode = mode

            log.info(f"cmd: {action}")

        except Exception as e:
            log.error(f"ERROR! by phone cmd : {e}")

    def _receive_commands(self, client_sock):
        buffer = ""
        while self._running:
            try:
                data = client_sock.recv(1024).decode("utf-8")
                if not data:
                    break
                buffer += data
                while "\n" in buffer:
                    line, buffer = buffer.split("\n", 1)
                    if line.strip():
                        self._handle_command(line)
            except Exception:
                break

    def _bluetooth_server(self):
        while self._running:
            server_sock = None
            client_sock = None
            try:
                server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
                server_sock.bind(("", bluetooth.PORT_ANY))
                server_sock.listen(1)

                port = server_sock.getsockname()[1]
                log.info(f"RFCOMM channel: {port}")

                bluetooth.advertise_service(
                    server_sock,
                    "SmartBike",
                    service_id=UUID,
                    service_classes=[UUID, bluetooth.SERIAL_PORT_CLASS],
                    profiles=[bluetooth.SERIAL_PORT_PROFILE],
                )

                log.info("Waiting phone connection...")
                client_sock, client_info = server_sock.accept()
                self._client_sock = client_sock
                log.info(f"Phone connected: {client_info}")
                self.buzzer.beep(times=1, duration=0.3)

                threading.Thread(
                    target=self._receive_commands,
                    args=(client_sock,),
                    daemon=True
                ).start()

                while self._running:
                    self.headlight.update(self.light.is_dark())
                    self.trip.update(self.speed.get_data()["current"])

                    payload = json.dumps(self._build_payload()) + "\n"
                    client_sock.send(payload.encode("utf-8"))

                    d = self._build_payload()
                    print(f"\r[GPS] {d['gps']['lat']},{d['gps']['lon']} | "
                          f"[Speed] {d['speed']['current']} km/h | "
                          f"[Light] {d['system']['light_level']} | "
                          f"[Head] {d['system']['headlight_mode']} | "
                          f"[Turn] {d['system']['turn_signal']}   ",
                          end='', flush=True)

                    time.sleep(1)

            except OSError as e:
                log.error(f"Connection broken: {e}")

            except Exception as e:
                log.error(f"ERROR! Bluetooth: {e}")
                time.sleep(2)

            finally:
                if client_sock:
                    try:
                        client_sock.close()
                    except Exception:
                        pass
                if server_sock:
                    try:
                        server_sock.close()
                    except Exception:
                        pass
                if self._running:
                    log.info("Waiting new connection...")


if __name__ == "__main__":
    bike = SmartBike()

    def signal_handler(sig, frame):
        print()
        bike.stop()
        sys.exit(0)

    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)

    bike.start()