#!/usr/bin/env python3

from winbox import *
import argparse

port = 8291
user = 'admin'
password = ''

def parse_args():
	parser = argparse.ArgumentParser(description='description')
	parser.add_argument('host', help = 'host name')
	parser.add_argument('filename', help = 'file name to download')
	parser.add_argument('-u', '--user', help = 'user name', required = False)
	parser.add_argument('-p', '--password', help = 'password', required = False)
	args = vars(parser.parse_args())
	return args

if __name__ == "__main__":
	args = parse_args()

	if args['user']:
		user = args['user']
	if args['password']:
		password = args['password']
	host_port = args['host'].split(':')
	host = host_port[0]
	if len(host_port) == 2:
		port = int(host_port[1])
	filename = args['filename']

	print('[*] Establishing a winbox session with %s:%s' % (host, port))
	winbox = mtWinboxSession(host, port)
	#if winbox.login(user.encode(), password.encode()):
	if winbox.login_cleartext(user.encode(), password.encode()):
		print('[+] Logged into %s:%s' % (host, port))
	else:
		print('[-] Login failed')
		winbox.close()
		exit(1)

	freq = mtFileRequest(winbox, filename.encode())
	if not freq.request_download():
		print('[-] File request failed [%s]' % (freq.error_description.decode()))
		winbox.close()
		exit(2)
	else:
		print('The "%s" file size is %s bytes' % (filename, freq.file_size))

	f = open(filename, 'wb')
	print('[*] Starting a download')
	download_data = freq.download()
	print('Received %s bytes' % len(download_data))
	f.write(download_data)
	f.close()
	winbox.close()
