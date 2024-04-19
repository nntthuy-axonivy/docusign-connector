package com.axonivy.connector.docusign.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.axonivy.connector.docusign.constant.DocuSignConstants;
import com.docusign.esign.model.Document;

import ch.ivyteam.ivy.application.IProcessModelVersion;
import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.ivy.workflow.ICase;
import ch.ivyteam.ivy.workflow.document.IDocument;

public class DocUtils {

	public static final String DEFAULT_DOC_ID = "1";

	public static Document ofLocalFile(String path) throws IOException, CoreException {
		IProcessModelVersion pmv = IProcessModelVersion.current();
		IFile localFile = pmv.getProject().getFile(path);
		try (InputStream is = localFile.getContents()) {
			return create(is, localFile.getName());
		}
	}

	public static Document ofIvyFile(File file) throws IOException {
		try (InputStream is = new FileInputStream(file.getJavaFile())) {
			return create(is, file.getName());
		}
	}

	public static Document create(InputStream is, String docName) throws IOException {
		byte[] docBytes = read(is);
		String docBase64 = new String(Base64.getEncoder().encode(docBytes));

		Document document = new Document();
		document.setDocumentBase64(docBase64);
		// can be different from actual file name
		document.setName(docName);
		// many different document types are accepted
		document.setFileExtension(StringUtils.substringAfterLast(docName, DocuSignConstants.DOT));
		// a label used to reference the doc
		document.setDocumentId(DEFAULT_DOC_ID);
		return document;
	}

	private static byte[] read(InputStream is) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			IOUtils.copy(is, bos);
			return bos.toByteArray();
		}
	}

	public static StreamedContent downloadIvyDoc(ICase selectedCase, IDocument document) throws IOException {
		return DefaultStreamedContent.builder()
        .stream(() -> selectedCase.documents().get(document.uuid()).read().asStream())
				.contentType(Files.probeContentType(document.read().asJavaFile().toPath())).name(document.getName())
				.build();
	}
}
