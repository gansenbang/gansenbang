#!/usr/bin/env python
from io.s4.manager.thrift import S4Manager
from io.s4.manager.thrift import constants

from thrift import Thrift
from thrift.transport import TSocket
from thirft.transport import TTransport
from thrift.protocol import TBinaryProtocol

class S4ManagerDriver:
	def __init__(self, host, port):
		try:
			transport = TSocket.TSocket(host,port)
			protocol = TBinaryProtocol.TBinaryProtocol(transport)
			transport.open()
			self.client = S4Manager.Client(protocol);
		except Thrift.TException, tx:
			raise tx

	def GetClient():
		return self.client
	# 增加一个机器集群
	# CreateCluster(self, zkAddress, clustername, machinelist):

	# 删除一个机器集群
	# RemoveCluster(self, clustername):

	# 获取所有的机器信息
	# GetAllMachinesList(self, ):

	# 获取当前所有的机器集群信息
	# GetAllClustersList(self, ):
	
	# 提交某个集群的cluster.xml
	# CommitS4ClusterXMLConfig(self, xmlfile, clustername):
	
	# 为某个s4集群内的小集群增加一个或多个s4server节点
	# AddS4Server(self, nodeconfig, clustername, s4clustername, adapterclustername):
	
	# 为某个s4集群内的小集群增加一个或多个client-adapter节点
	# AddClientAdapter(self, nodeconfig, clustername, s4clustername, listenappname):
	
	# 删除某个s4小集群内的某个节点（包括s4server和client-adapter）
	# RemoveS4Node(self, clustername, s4clustername, hostport):

	# 获取某个机器集群内关于S4小集群的所有信息
	# GetS4ClusterMessage(self, clustername):

	# 把某个s4集群内的小集群启动为s4server
	# StartS4ServerCluster(self, clustername, s4clustername, adapterclustername):

	# 把某个s4集群内的小集群启动为client-adpater
	# StartClientAdapterCluster(self, clustername, s4clustername, listenappname):

	# 删除s4集群内的某个小集群（包括s4server和client-adapter）
	# RemoveS4Cluster(self, clustername, s4clustername):
	
	# 删除整个s4集群
	# RemoveAllS4Cluster(self, clustername):

	# 恢复或启动某个节点，启动为s4server
	# RecoveryS4Server(self, clustername, s4clustername, s4adaptername, hostport):

	# 恢复或启动某个节点，启动为client-adpater
	# RecoveryClientServer(self, clustername, s4clustername, listenappname, hostport):
