package com.axonivy.connector.docusign;

import com.docusign.esign.model.SignHere;

/**
 * @author This class is deprecated, please use com.axonivy.connector.docusign.util.SignUtils
 *
 */
@Deprecated(forRemoval = true, since = "11.3")
public class SignUtils extends com.axonivy.connector.docusign.util.SignUtils {

	public static SignHere simple(String label) {
		SignHere signHere = new SignHere();
		signHere.setDocumentId("1");
		signHere.setPageNumber("1");
		signHere.setRecipientId("1");
		signHere.setTabLabel(label);
		signHere.setXPosition("10");
		signHere.setYPosition("10");
		return signHere;
	}
}
