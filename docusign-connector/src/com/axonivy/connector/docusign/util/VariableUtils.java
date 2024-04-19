package com.axonivy.connector.docusign.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.axonivy.connector.docusign.constant.DocuSignConstants;
import com.axonivy.connector.docusign.constant.DocuSignVariables;

import ch.ivyteam.ivy.environment.Ivy;

public class VariableUtils {

	private VariableUtils() {
	}

	public static List<String> getVariableAsListString(String variableName) {
		String variableValue = Ivy.var().get(variableName);
		if (variableValue.contains(DocuSignConstants.COMMA)) {
			return Arrays.asList(variableValue.split(DocuSignConstants.COMMA)).stream().map(data -> data.trim())
					.collect(Collectors.toList());
		}
		return List.of(variableValue);
	}

	public static String getReturnPage() {
		return Ivy.var().get(DocuSignVariables.RETURN_PAGE);
	}

	public static List<String> getFrameAncestors() {
		return getVariableAsListString(DocuSignVariables.FRAME_ANCESTORS);
	}

	public static List<String> getMessageOrigins() {
		return getVariableAsListString(DocuSignVariables.MESSAGE_ORIGINS);
	}
}
