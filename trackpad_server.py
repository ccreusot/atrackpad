#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Author: calimeraw
# @Date:   2014-03-21 11:18:05
# @Last Modified by:   calimeraw
# @Last Modified time: 2014-04-22 11:08:57
import socket
import select
import signal
from xdrlib import Unpacker
from pymouse import PyMouse


class ServerMouseController:

    """

    class ServerMouseController
    ---
    This class should be able to control the mouse with the data received
    from a client. It should be able to move, scroll and click. It works only
    with one client.

    """
    PACKET_MOVE = 1
    PACKET_CLICK = 2
    PACKET_SCROLL = 3

    # We want only one connection for the server.
    MAX_CONNECTION = 1

    def __init__(self, port=34555):
        print ('You should connect on', (
            socket.gethostbyname_ex(socket.gethostname()), port))
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.setsockopt(
            socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.mouse_controller = PyMouse()
        self.server_running = True
        self.host = ''
        self.port = port
        self.buffer = ''  # array.array('b', '\0' * 512)
        self.client = None

    def start(self):
        def handler_interupt(signum, frame):
            print('interrupt server')
            self.stop()
        self.server_socket.bind((self.host, self.port))
        self.server_socket.listen(ServerMouseController.MAX_CONNECTION)
        signal.signal(signal.SIGINT, handler_interupt)
        print('start server')
        self.run()

    def run(self):
        while self.server_running:
            potential_read = [self.server_socket]
            if self.client is not None:
                potential_read.append(self.client)
            try:
                ready_to_read, ready_to_write, in_erro = select.select(
                    potential_read, [], [])
                if self.server_socket in ready_to_read:
                    conn, addr = self.server_socket.accept()
                    self.client = conn
                    print('New connection from ', addr)
                elif self.client in ready_to_read:
                    # self.client.recv_into(self.buffer, 512)
                    recv = self.client.recv(128)
                    self.buffer += recv
                    if len(recv) == 0:
                        print('Disconnection from client')
                        self.client.close()
                        self.client = None
                        self.buffer = ''
                        continue
                    unpack = Unpacker(self.buffer)
                    if len(self.buffer) >= unpack.unpack_int():
                        unpack.set_position(0)
                        size = unpack.unpack_int()
                        cmd = unpack.unpack_int()
                        if cmd == ServerMouseController.PACKET_MOVE:
                            # Mouse move control
                            x = unpack.unpack_float()
                            y = unpack.unpack_float()
                            print(size, cmd, x, y)
                            self.mouse_controller.move(
                                self.mouse_controller.position()[0] - x,
                                self.mouse_controller.position()[1] - y)
                        elif cmd == ServerMouseController.PACKET_CLICK:
                            # Mouse click control
                            button = unpack.unpack_int()
                            nb_click = unpack.unpack_int()
                            print(size, cmd, button, nb_click)
                            self.mouse_controller.click(
                                self.mouse_controller.position()[0],
                                self.mouse_controller.position()[1],
                                button,
                                nb_click)
                        elif cmd == ServerMouseController.PACKET_SCROLL:
                            # Mouse scrolling
                            x = unpack.unpack_float()
                            y = unpack.unpack_float()
                            print(size, cmd, x, y)
                            self.mouse_controller.scroll(
                                vertical=int(y), horizontal=int(x))
                        self.buffer = self.buffer[unpack.get_position():]
            except select.error as e:
                print(e)
        if self.client is not None:
            self.client.close()
        self.server_socket.close()
        print('Server stop')

    def stop(self):
        self.server_running = False

if __name__ == '__main__':
    server = ServerMouseController()
    server.start()
