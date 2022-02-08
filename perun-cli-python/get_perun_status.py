#!/usr/bin/env python3
# -*- coding: utf8 -*-
import sys
from cliutils import PerunRpc, options

def main(args):
	"""main function"""

	rpc = PerunRpc(args)

	# get VO by name
	status = rpc.utils().get_perun_status()
	for line in status:
		print(line)

# calling main when invoked from CLI
if __name__ == "__main__":
	options = options("Outputs Perun status")
	sys.exit(main(vars(options.parse_args())) or 0)
