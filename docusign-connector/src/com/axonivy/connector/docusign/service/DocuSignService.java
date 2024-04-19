package com.axonivy.connector.docusign.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.axonivy.connector.docusign.bo.DownloadResult;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.process.call.SubProcessCall;
import ch.ivyteam.ivy.process.call.SubProcessCallResult;
import ch.ivyteam.ivy.scripting.objects.File;

public class DocuSignService {
	
	private static final String ERROR_PARAM = "error";
	private static final String SERVICE_BASE = "Processes/";
	public static final String ENVELOPES_PROCESS = SERVICE_BASE + "Envelopes";
	public static final String GET_SIGNED_DOC_CONTENT = "getSignedDocContentStream(String,String)";
	public static final String ENVELOPE_ID = "envelopeId";
	public static final String SIGNED_DOCUMENT_ID="signedDocumentId";
	public static final String SIGNED_DOCUMENT_ENTITY = "signedDocumentEntity";

	public static DownloadResult getSignedDocContentStream(String envelopeId, String signedDocumentId) {
		SubProcessCallResult callResult = SubProcessCall.withPath(ENVELOPES_PROCESS).withStartSignature(GET_SIGNED_DOC_CONTENT)
				.withParam(ENVELOPE_ID, envelopeId)
				.withParam(SIGNED_DOCUMENT_ID, signedDocumentId)
				.call();
		handleError(callResult);
		return callResult.get(SIGNED_DOCUMENT_ENTITY, DownloadResult.class);
	}

	private static void handleError(SubProcessCallResult callResult) {
		BpmError error = callResult.get(ERROR_PARAM, BpmError.class);
		if(Objects.nonNull(error)) {
			throw error;
		}
	}

	public static DownloadResult download(WebTarget target, String filename, boolean asFile) {
		DownloadResult result = new DownloadResult();
		try(Response response = target.request().get()) {
			result = download(response, filename, asFile);
		}
		
		return result;
	}

	public static DownloadResult download(Response response, String filename, boolean asFile) {
		DownloadResult result = new DownloadResult();
		
		if(response.getStatus() == Response.Status.OK.getStatusCode()) {
			try(InputStream is = response.readEntity(InputStream.class)) {
				byte[] bytes = IOUtils.toByteArray(is);
				result.setFilename(filename);
				if(asFile) {
					File file = new File(filename, true);
					FileUtils.writeByteArrayToFile(file .getJavaFile(), bytes);
					result.setFile(file);
				} else {
					result.setContent(bytes);
				}
			} catch (IOException e) {
				result.setError(BpmError.create("com.axonivy.connector.docusign.download.doc")
						.withMessage(e.getMessage())
						.withCause(e).build());
			}
		}
		
		return result;
	}
}
