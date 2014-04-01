package cz.metacentrum.perun.webgui.widgets.recaptcha;

/**
 * This Class contains all needed Variables for a custom translation of the
 * reCAPTCHA widget
 *
 * Original source code was taken from: http://code.google.com/p/gwt-recaptcha/
 * ORIGINAL LICENSE: Apache License 2.0
 *
 * @author Dominik Duda
 */
public class RecaptchaTranslation {

	private String instructionVisual;
	private String instructionAudio;
	private String playAgain;
	private String cantHereThis;
	private String visualChalange;
	private String audioChalange;
	private String refreshButton;
	private String helpButton;
	private String incorrectTryAgain;

	public RecaptchaTranslation(String instructionVisual, String instructionAudio,
			String playAgain, String cantHereThis, String visualChalange,
			String audioChalange, String refreshButton, String helpButton,
			String incorrectTryAgain) {
		super();

		this.instructionVisual = instructionVisual;
		this.instructionAudio = instructionAudio;
		this.playAgain = playAgain;
		this.cantHereThis = cantHereThis;
		this.visualChalange = visualChalange;
		this.audioChalange = audioChalange;
		this.refreshButton = refreshButton;
		this.helpButton = helpButton;
		this.incorrectTryAgain = incorrectTryAgain;
	}

	public String getInstructionVisual() {
		return instructionVisual;
	}

	public String getInstructionAudio() {
		return instructionAudio;
	}

	public String getPlayAgain() {
		return playAgain;
	}

	public String getCantHereThis() {
		return cantHereThis;
	}

	public String getVisualChalange() {
		return visualChalange;
	}

	public String getAudioChalange() {
		return audioChalange;
	}

	public String getRefreshButton() {
		return refreshButton;
	}

	public String getHelpButton() {
		return helpButton;
	}

	public String getIncorrectTryAgain() {
		return incorrectTryAgain;
	}
}
