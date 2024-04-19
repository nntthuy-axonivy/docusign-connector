package com.axonivy.connector.docusign.constant;

public class DocuSignConstants {
	public static final String DOT = ".";
	public static final String COMMA = ",";
	public static final String SEMICOLON = "; ";
	public static final String UNDERSCORE = "_";

	public static final String PDF_EXTENSION = ".pdf";
	public static final String ENVELOPE_ID = "envelopeId";
	public static final String TYPE_AS_CONTENT = "content";
	public static final String UPLOADED_PREFIX = "UPLOADED" + UNDERSCORE;
	public static final String SIGNED_SUFFIX = UNDERSCORE + "SIGNED";
	public static final String DEFAULT_ENVELOPE_STATUS = "sent";
	public static final int DEFAULT_ANCHOR_TEXT_LENGTH = 50;
	public static final String DEFAULT_PAGE_NUMBER = "1";
	public static final String DEFAULT_X_OFFSET = "50";
	public static final String DEFAULT_Y_OFFSET = "100";
	public static final String DEFAULT_ROLE_NAME = "signer";
	public static final String DEFAULT_AUTHEN_METHOD = "none";

	public static final String HIDE = "HIDE";
	public static final String EVENT = "event";
	public static final String SIGNING_COMPLETE = "signing_complete";
	public static final String IVY_TOKEN = "ivyToken";
	public static final String REQUEST_IVY_TOKEN = "requestIvyToken";
	public static final String CALL_BACK_TASK_ID = "callBackTaskId";
}
