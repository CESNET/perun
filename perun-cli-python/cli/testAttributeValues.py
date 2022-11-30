from perun_openapi import ApiException
from rich import print
from cli import PerunException
import typer
import cli


def test_attribute_values(user_id: int = typer.Option(3197, '-u', '--user_id', help='user ID'),
						  vo_name: str = typer.Option("meta", '-v', '--voShortName', help='short name of VO')) -> None:
	""" tests getting all possible attribute value types """
	rpc = cli.rpc
	try:
		user = rpc.users_manager.get_user_by_id(user_id)
		vo = rpc.vos_manager.get_vo_by_short_name(vo_name)
		member = rpc.members_manager.get_member_by_user(vo=vo.id, user=user.id)
		attr = rpc.attributes_manager.get_member_attribute_by_name(member.id, "urn:perun:member:attribute-def:def:membershipExpiration")
		print(attr['namespace']+':'+attr['friendlyName'], attr['type'], ':', attr['value'])
		for attrName in ["urn:perun:user:attribute-def:def:preferredMail",
						 "urn:perun:user:attribute-def:def:sshPublicKey",
						 "urn:perun:user:attribute-def:def:publications",
						 "urn:perun:user:attribute-def:virt:loa",
						 "urn:perun:user:attribute-def:def:it4iBlockCollision",
						 "urn:perun:user:attribute-def:def:address"]:
			attr = rpc.attributes_manager.get_user_attribute_by_name(user.id, attrName)
			print()
			print(attr['namespace']+':'+attr['friendlyName'], attr['type'], ':', attr['value'])
	except ApiException as ex:
		print('error name:', PerunException(ex).name)
		print('error message:', PerunException(ex).message)
		raise typer.Exit(code=1)

