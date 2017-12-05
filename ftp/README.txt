Server：
参数： ftp 服务器提供的文件路径

Client：
命令:
	[1]	ls	服务器返回当前目录文件列表（<file/dir>	name	size）
	[2]	cd  <dir>	进入指定目录（需判断目录是否存在，并给出提示）
	[3]	get  <file>	通过UDP下载指定文件，保存到客户端当前目录下
	[4]	bye	断开连接，客户端运行完毕
