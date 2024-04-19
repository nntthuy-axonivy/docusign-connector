package com.axonivy.connector.docusign.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.connector.docusign.auth.OAuth2Feature.Property;
import com.axonivy.connector.docusign.event.EnvelopeCompleted;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClients;
import ch.ivyteam.ivy.rest.client.security.CsrfHeaderFeature;
import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.ivy.security.ISession;
import ch.ivyteam.ivy.workflow.ICase;
import ch.ivyteam.ivy.workflow.ITask;
import ch.ivyteam.ivy.workflow.TaskState;
import ch.ivyteam.ivy.workflow.WorkflowNavigationUtil;

@IvyProcessTest(enableWebServer = true)
public class TestDocuSignDemo {

  @BeforeEach
  void beforeEach(AppFixture fixture, IApplication app) throws Exception {
    fixture.config("RestClients.'DocuSign (DocuSign REST API)'.Url", DocuSignServiceMock.URI);
    var clients = RestClients.of(app);
    var docuSign = clients.find(EnvelopeCompleted.DOCU_SIGN_CLIENT_ID);
    var mockClient = mockClient(docuSign);
    clients.set(mockClient);
  }

  private static RestClient mockClient(RestClient docuSign) throws URISyntaxException {
    Path testKeyFile = Path.of(TestDocuSignDemo.class.getResource("testKey.pem").toURI());
    var mockClient = docuSign.toBuilder()
      .feature(CsrfHeaderFeature.class.getName())
      .property(Property.INTEGRATION_KEY, "test-key")
      .property(Property.SECRET_KEY, "not-my-secret")
      .property(Property.JWT_USE, Boolean.FALSE.toString())
      .property(Property.JWT_USER_ID, "test-user")
      .property(Property.JWT_KEY_FILE, testKeyFile.toAbsolutePath().toString())
      .property(Property.AUTH_BASE_URI, DocuSignServiceMock.URI + "/oauth")
      .property("PATH.accountId", "placeholder")
      .toRestClient();
    return mockClient;
  }

  @Test
  public void main(BpmClient bpmClient, ISession session, IApplication app) throws Exception {
    ExecutionResult result = userFlow(bpmClient, session);
    com.axonivy.connector.docusign.connector.demo.Data docuSign = result.data().last();
    assertThat(docuSign.getEnvelopes()).hasSize(1);

    ITask signTask = result.workflow().activeTasks().get(0);
    assertThat(signTask.getState())
      .isEqualTo(TaskState.SUSPENDED);

    ITask system2Signing = result.workflow().activeTasks().stream()
      .filter(task -> Objects.equals(task.getActivatorName(), "#SYSTEM"))
      .findFirst().orElseThrow();
    assertThat(system2Signing.getState()).isEqualTo(TaskState.SUSPENDED);
    bpmClient.start()
      .task(system2Signing)
      .as().systemUser()
      .execute();

    ITask wait4Signing = result.workflow().activeTasks().get(1);
    assertThat(wait4Signing.getState())
      .isEqualTo(TaskState.WAITING_FOR_INTERMEDIATE_EVENT);
    fireIntermediateEvent(app, wait4Signing, docuSign.getEnvelopeId());

    assertThat(wait4Signing.getState()).isEqualTo(TaskState.SUSPENDED);
    ExecutionResult endResult = bpmClient.start()
      .task(wait4Signing)
      .as().systemUser()
      .execute();

    assertThat(endResult.bpmError())
      .isNull();
    assertThat(result.http().redirectLocation())
      .contains("?endedTaskId=");
    assertThat(wait4Signing.getState())
      .isEqualTo(TaskState.DONE);

    ITask completedTask = result.workflow().activeTasks().get(1);
    assertThat(completedTask.getState())
      .isEqualTo(TaskState.SUSPENDED);

    ICase activeCase = endResult.workflow().activeCase();
    assertThat(activeCase.documents().getAll()).isNotEmpty();
  }


  private ExecutionResult userFlow(BpmClient bpmClient, ISession session) throws IOException {
    File doc = new File("sampledDoc.pdf", false);
    doc.createNewFile();
    bpmClient.mock()
      .uiOf(BpmProcess.name("DemoESign").elementName("Upload Document"))
      .with((params, results) -> results.set("file", doc));
    ExecutionResult result = bpmClient.start()
      .process("DemoESign/startWf.ivp")
      .as().session(session)
      .execute();

    assertThat(result.http().redirectLocation()).containsSubsequence("http://localhost:",
      "/test/api/docuSignMock/oauth/auth?",
      "response_type=code&scope=signature+impersonation&client_id=test-key&redirect_uri=http%3A%2F%2Flocalhost%3A",
      "%2Foauth2%2Fcallback");
    ExecutionResult result2 = bpmClient.start()
      .task(result.workflow().executedTask())
      .withParam("code", "a-test-code")
      .as().session(session)
      .execute();
    return result2;
  }

  private void fireIntermediateEvent(IApplication app, ITask waitTask, String envelopeId) {
    var element = waitTask.getIntermediateEvent().getIntermediateEventElement();
    var workflowContext = WorkflowNavigationUtil.getWorkflowContext(app);
    workflowContext.fireIntermediateEvent(element, envelopeId, envelopeId, "test-event");
  }

}
