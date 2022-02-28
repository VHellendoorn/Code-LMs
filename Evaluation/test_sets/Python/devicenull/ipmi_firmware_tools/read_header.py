#!/usr/bin/python

import re, hashlib, os, io, argparse, sys, zlib
from ConfigParser import ConfigParser
from ipmifw.FirmwareImage import FirmwareImage
from ipmifw.FirmwareFooter import FirmwareFooter

cmdparser = argparse.ArgumentParser(description='Read and extract data from SuperMicro IPMI firmware')
cmdparser.add_argument('--extract',action='store_true',help='Extract any detected firmware images')
cmdparser.add_argument('filename',help='Filename to read from')
args = cmdparser.parse_args()

default_ini = """
[flash]
total_size=0

[global]
major_version=0
minor_version=0
footer_version=2
type=unknown

[images]
"""

config = ConfigParser()
config.readfp(io.BytesIO(default_ini))

with open(args.filename,'r') as f:
	ipmifw = f.read()

config.set('flash', 'total_size', len(ipmifw))

try:
	os.mkdir('data')
except OSError:
	pass

print "Read %i bytes" % len(ipmifw)

fwtype = 'unknown'
if len(ipmifw) > 0x01fc0000:
	if ipmifw[0x01fc0000:0x01fc0005] == '[img]':
		fwtype = 'aspeed'

if fwtype == 'unknown':
	bootloader = ipmifw[:64040]
	bootloader_md5 = hashlib.md5(bootloader).hexdigest()

	if bootloader_md5 != "166162c6c9f21d7a710dfd62a3452684":
		print "Warning: bootloader (first 64040 bytes of file) md5 doesn't match.  This parser may not work with a different bootloader"
		print "Expected 166162c6c9f21d7a710dfd62a3452684, got %s" % bootloader_md5
	else:
		print "Bootloader md5 matches, this parser will probably work!"
		fwtype = 'winbond'

	if args.extract:
		print "Dumping bootloader to data/bootloader.bin"
		with open('data/bootloader.bin','w') as f:
			f.write(bootloader)

config.set('global', 'type', fwtype)

if fwtype == 'winbond':
	from ipmifw.Winbond import Winbond

	firmware = Winbond()
	firmware.parse(ipmifw, args.extract, config)

elif fwtype == 'aspeed':
	from ipmifw.ASpeed import ASpeed

	config.set('global', 'footer_version', 3)
	firmware = ASpeed()
	firmware.parse(ipmifw, args.extract, config)

else:
	print "Error: Unable to determine what type of IPMI firmware this is!"
	sys.exit(1)


if args.extract:
	with open('data/image.ini','w') as f:
		config.write(f)
else:
	print "\nConfiguration info:\n"
	config.write(sys.stdout)
