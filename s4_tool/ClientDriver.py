#!/usr/bin/env python

from io.s4.logger import S4Monitor
from io.s4.logger.ttypes import *

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol



class ClientDriver:
	def __init__(self,host,port):
		try:
			transport = TSocket.TSocket(host,port)
			protocol = TBinaryProtocol.TBinaryProtocol(transport)
			transport.open()
			self.client = S4Monitor.Client(protocol)
		except Thrift.TException, tx:
			raise tx

	
	def monitor(self):
		return self.client.monitor()

if __name__ == "__main__":
	client = ClientDriver('localhost',7791)
	print client.monitor()

