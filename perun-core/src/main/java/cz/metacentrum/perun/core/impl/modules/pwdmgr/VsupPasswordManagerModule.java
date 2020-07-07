package cz.metacentrum.perun.core.impl.modules.pwdmgr;

public class VsupPasswordManagerModule extends GenericPasswordManagerModule {

	public VsupPasswordManagerModule() {

		// override random password generating params
		this.randomPasswordLength = 14;

		// omit chars that can be mistaken by users: yY, zZ, O, l, I and all spec chars.
		this.randomPasswordCharacters = "ABCDEFGHJKLMNPQRSTUVWXabcdefghijkmnopqrstuvwx0123456789".toCharArray();

	}

}
