#!/usr/bin/env python3
# -*- coding: utf8 -*-

# example of using Perun OpenAPI

from __future__ import print_function
import time
import os
import sys
import argparse
import perun_openapi
from perun_openapi.rest import ApiException
from pprint import pprint

class MyConfiguration(perun_openapi.configuration.Configuration):
    def auth_settings(self):
        """Overrides Auths generated from openapi.yml because the Python client uses them all at once
        """
        return {
            'BasicAuth':
                {
                    'type': 'basic',
                    'in': 'header',
                    'key': 'Authorization',
                    'value': self.get_basic_auth_token()
                },
        }

def main(args):
    """main function"""
 
    config = MyConfiguration()
    # configure debugging
    config.debug=args['debug']
    # configure URL
    if os.getenv('PERUN_URL'): 
        config.host = os.getenv('PERUN_URL')
    if args['PERUN_URL']:
        config.host = args['PERUN_URL']

    # configure authentication
    perun_user = None
    if os.getenv('PERUN_USER'): 
        perun_user = os.getenv('PERUN_USER')
    elif args['PERUN_USER']:
        perun_user =  args['PERUN_USER']
    else:
        print("user for authentication is unknown, user PERUN_USER env var or -P/--PERUN_USER option", file=sys.stderr)
        return 1
    sa = perun_user.split('/',maxsplit=1)
    if len(sa)<2 :
        print("PERUN_USER must have format username/password", file=sys.stderr)
        return 1
    config.username = sa[0]
    config.password = sa[1]

    # create API client
    apiClient = perun_openapi.api_client.ApiClient(config)
    apiClient.user_agent = "Perun OpenAPI Python CLI"
    
    # get manager
    usersManager = perun_openapi.api.UsersManagerApi(apiClient)
    
    userId = args['user']
    try:
        api_response = usersManager.get_user_by_id(userId)
        pprint(api_response)
    except ApiException as e:
        print("Exception when calling : %s\n" % e)

#calling main when invoked from CLI
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Prints Perun user.')
    parser.add_argument('-D','--debug', help='debugging output', action='store_const', const=True, default=False, required=False )
    parser.add_argument('-U','--PERUN_URL', help='URL of Perun RPC', required=False )
    parser.add_argument('-P','--PERUN_USER', help='user/password', required=False )
    parser.add_argument('-id','--user', help='numeric id of user', type=int, required=True )
    sys.exit(main(vars(parser.parse_args())) or 0)
