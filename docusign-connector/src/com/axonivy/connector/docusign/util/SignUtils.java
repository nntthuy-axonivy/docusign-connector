package com.axonivy.connector.docusign.util;

import static com.axonivy.connector.docusign.constant.DocuSignConstants.CALL_BACK_TASK_ID;
import static com.axonivy.connector.docusign.constant.DocuSignConstants.DEFAULT_ROLE_NAME;
import static com.axonivy.connector.docusign.constant.DocuSignConstants.EVENT;
import static com.axonivy.connector.docusign.constant.DocuSignConstants.IVY_TOKEN;
import static com.axonivy.connector.docusign.constant.DocuSignConstants.REQUEST_IVY_TOKEN;
import static com.axonivy.connector.docusign.constant.DocuSignConstants.SIGNING_COMPLETE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.UUID;

import com.docusign.esign.model.Signer;
import com.fasterxml.jackson.databind.JsonNode;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.ISession;
import ch.ivyteam.ivy.security.IUser;
import ch.ivyteam.ivy.workflow.ITask;

public class SignUtils {

	public static final String DEFAULT_SIGNATURE_END_PAGE = "Processes/ESign/signatureReturn.ivp";
	public static final String DEFAULT_IFRAME_END_PAGE = "/page/docusign-connector$1/signatureReturn.jsp";
	private static final String SIGNING_CALL_BACK_URL_PATTERN = "%s?" + CALL_BACK_TASK_ID + "=%s&" + IVY_TOKEN + "=%s";

	public static Signer signer(ISession session) {
		return signer(session.getSessionUser());
	}

	public static Signer myself(ISession session) {
		Signer me = new Signer();
		JsonNode userInfo = com.axonivy.connector.docusign.auth.UserUriFilter.readUserInfo(session);
		me.setName(userInfo.get("name").asText());
		me.setEmail(userInfo.get("email").asText());
		me.recipientId(Long.toString(session.getIdentifier()));
		return me;
	}

	public static Signer getSessionUserAsFirstSigner(ISession session) {
		Signer signer = signer(session.getSessionUser());
		signer.clientUserId(session.getSessionUser().getSecurityMemberId());
		signer.recipientId("1");
		signer.routingOrder("1");
		signer.setRoleName(DEFAULT_ROLE_NAME);
		return signer;
	}

	public static boolean isSigningCompleted(ITask requestTask) {
		boolean isSignCompleted = requestTask.customFields().stringField(EVENT).getOrDefault(EMPTY)
				.contentEquals(SIGNING_COMPLETE)
				&& requestTask.customFields().stringField(IVY_TOKEN).getOrDefault(EMPTY)
						.contentEquals(requestTask.customFields().stringField(REQUEST_IVY_TOKEN).getOrDefault(EMPTY));
		return isSignCompleted;
	}

	public static Signer signer(IUser user) {
		Signer signer = new Signer();
		signer.recipientId(user.getSecurityMemberId());
		signer.setEmail(user.getEMailAddress());
		signer.setName(user.getFullName());
		return signer;
	}

	public static String getDefaultRemoteSigningReturnPage(ITask runningTask) {
		String ivyToken = generateNewIvyToken(runningTask);
		return String.format(SIGNING_CALL_BACK_URL_PATTERN, Ivy.html().startRef(DEFAULT_SIGNATURE_END_PAGE),
				runningTask.getId(), ivyToken);
	}

	public static String getDefaultIFrameSigningReturnPage(ITask runningTask) {
		String ivyToken = generateNewIvyToken(runningTask);
		return String.format(SIGNING_CALL_BACK_URL_PATTERN, Ivy.html().applicationHomeRef() + DEFAULT_IFRAME_END_PAGE,
				runningTask.getId(), ivyToken);
	}

	private static String generateNewIvyToken(ITask runningTask) {
		String ivyToken = UUID.randomUUID().toString();
		runningTask.customFields().stringField(REQUEST_IVY_TOKEN).set(ivyToken);
		return ivyToken;
	}
}
