package cz.metacentrum.perun.webgui.widgets.recaptcha;

/**
 * ReCaptcha handles reCaptcha object itself
 *
 * Original source code was taken from: http://code.google.com/p/gwt-recaptcha/
 * ORIGINAL LICENSE: Apache License 2.0
 *
 * @author Claudius Hauptmann <claudiushauptmann.com@googlemail.com>
 */
public class Recaptcha {

	public static final String IMAGE = "image";
	public static final String AUDIO = "audio";

	public static native void create(String key, String div,
			String theme, String lang, int tabIndex) /*-{
				$wnd.Recaptcha.create(key, "recaptcha_div", {
theme:theme,
lang:lang,
tabindex:tabIndex
});
}-*/;

public static native void create(String key, String div,
		String theme, String lang, int tabIndex,
		String customTheme) /*-{
			$wnd.Recaptcha.create(key, "recaptcha_div", {
theme:theme,
lang:lang,
tabindex:tabIndex,
custom_theme_widget:customTheme
});
}-*/;

public static native void create(String key, String div,
		String theme, String lang, int tabIndex,
		String instructionVisual, String instructionAudio, String playAgain, String cantHereThis, String visualChalange, String audioChalange, String refreshButton, String helpButton, String incorrectTryAgain) /*-{
			$wnd.Recaptcha.create(key, "recaptcha_div", {
theme:theme,
lang:lang,
tabindex:tabIndex,
custom_translations:{
instructions_visual : instructionVisual,
instructions_audio : instructionAudio,
play_again : playAgain,
cant_hear_this : cantHereThis,
visual_challenge : visualChalange,
audio_challenge : audioChalange,
refresh_btn : refreshButton,
help_btn : helpButton,
incorrect_try_again : incorrectTryAgain
}
});
}-*/;

public static native void create(String key, String div,
		String theme, String lang, int tabIndex,
		String customTheme, String instructionVisual,
		String instructionAudio, String playAgain,
		String cantHereThis, String visualChalange,
		String audioChalange, String refreshButton,
		String helpButton, String incorrectTryAgain) /*-{
			$wnd.Recaptcha.create(key, "recaptcha_div", {
theme:theme,
lang:lang,
tabindex:tabIndex,
custom_translations:{
instructions_visual : instructionVisual,
instructions_audio : instructionAudio,
play_again : playAgain,
cant_hear_this : cantHereThis,
visual_challenge : visualChalange,
audio_challenge : audioChalange,
refresh_btn : refreshButton,
help_btn : helpButton,
incorrect_try_again : incorrectTryAgain
},
custom_theme_widget:customTheme
});
}-*/;

public static native void reload() /*-{
	$wnd.Recaptcha.reload();
}-*/;

public static native void destroy() /*-{
	$wnd.Recaptcha.destroy();
}-*/;

public static native String getChallenge() /*-{
	return $wnd.Recaptcha.get_challenge();
}-*/;

public static native String getResponse() /*-{
	return $wnd.Recaptcha.get_response();
}-*/;

public static native void focusResponseField() /*-{
	return $wnd.Recaptcha.focus_response_field();
}-*/;

public static native void showHelp() /*-{
	return $wnd.Recaptcha.showhelp();
}-*/;

public static native void switchType(String newType) /*-{
	return $wnd.Recaptcha.switch_type(newType);
}-*/;
}
