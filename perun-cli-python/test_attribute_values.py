#!/usr/bin/env python3
# -*- coding: utf8 -*-
import sys
from cliutils import PerunRpc, options
from pprint import pprint

def main(args):
    """main function"""

    rpc = PerunRpc(args)
    user = rpc.users_manager().get_user_by_id(3197)
    member = rpc.members_manager().get_member_by_user(vo=21, user=user.id)


    attr = rpc.attributes_manager().get_member_attribute_by_name(member.id, "urn:perun:member:attribute-def:def:membershipExpiration")
    print(attr['namespace']+':'+attr['friendlyName'], attr['type'], ':', attr['value'])

    for attrName in ["urn:perun:user:attribute-def:def:preferredMail",
                     "urn:perun:user:attribute-def:def:sshPublicKey",
                     "urn:perun:user:attribute-def:def:publications",
                     "urn:perun:user:attribute-def:virt:loa",
                     "urn:perun:user:attribute-def:def:it4iBlockCollision",
                     "urn:perun:user:attribute-def:def:address"]:
        attr = rpc.attributes_manager().get_user_attribute_by_name(user.id, attrName)
        print()
        print(attr['namespace']+':'+attr['friendlyName'], attr['type'], ':', attr['value'])


# calling main when invoked from CLI
if __name__ == "__main__":
    options = options("Just test, no options")
    sys.exit(main(vars(options.parse_args())) or 0)
