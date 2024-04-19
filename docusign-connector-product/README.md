# DocuSign Connector
Axon Ivy's [DocuSign] (https://www.docusign.com/products/electronic-signature)
connector helps you to accelerate process automation initiatives by integrating eSignatures into your process application within no time. DocuSign eSignature accelerates agreements, eliminates manual tasks, and makes it easy to connect with the tools and systems you're already using. From sales contracts and offer letters to account openings and invoices, DocuSign eSignature is the world's #1 way to send and sign from practically anywhere, at any time. This connector:

-	Is based on REST web service technologies.
-	Provides access to the core features of DocuSign eSignature to virtually sign all kinds of documents.
-	Supports you with an easy-to-copy demo implementation to reduce your integration effort.
-	Enables low-code citizen developers to enhance existing business processes with electronic signature features.


## Demo

1. Upload a document and assign **signers** for it.  
![signing-process](images/eSignDocumentProcess.png)

1. Signers will be involved by an e-mail into the web-based signing flow.  
![place-signature](images/docuSign_finish.png)

## Setup

Before any signing interactions between the Axon Ivy Engine and the DocuSign eSignature services can be run, they have to be introduced to each other. This can be done as follows:

1. Create a free DocuSign developer account: https://account-d.docusign.com/#/username
2. Create a new `application` at https://admindemo.docusign.com/authenticate?goTo=apiIntegratorKey
   - Note the **User ID**.
   - The **API Account ID**. 
   ![create-app](images/appsAndKeys.png)
3. Edit the created application:
   - Note the **Integration Key**
   - Scroll to **Authentication** choose `Authorization Code Grant`, click `Add Secret Key`,
     and note the **Secret Key**
   - Scroll to **Additional settings** and configure a `Redirect URI` to Axon Ivy.
     The redirect URI must point to the Axon Ivy authentication callback URI `.../oauth2/callback`. 
	 For the Axon Ivy Designer, this is normally `http://localhost:8081/oauth2/callback`.
   - Save the changed application settings.  
   ![edit-app](images/application.png)

4. Run `start.ivp` of the DemoESign demo process to test your setup. Your setup was correct,
   if you are being asked to authorize yourself with a DocuSign account.  
   ![docusign-auth](images/docuSign_auth.png)
   
5. Obtain consent endpoint:

   You can redirect a user’s browser window to the GET `/oauth/auth` endpoint to obtain consent. This is the first step in several authentication scenarios. It has different functions when supplied with different parameters.
    
   When you navigate to it in a browser, you can use this endpoint to:
    
    *    Obtain individual or admin consent in any of the authentication scenarios.
    *    Obtain an authorization code for the Authorization Code Grant.
    *    Obtain an access token directly, using the Implicit Grant.

   The syntax and parameters used for calling this endpoint in your browser are shown below:
   ```
   https://account-d.docusign.com/oauth/auth?
        response_type=CODE_OR_TOKEN
        &scope=YOUR_REQUESTED_SCOPES
        &client_id=YOUR_INTEGRATION_KEY
        &state=YOUR_CUSTOM_STATE
        &redirect_uri=YOUR_REDIRECT_URI
    ```
    After a successful call, the Authentication Service verifies that the client application is valid and has access to the requested scope. If so, it returns the requested data to the provided redirect URI as a query parameter:

    *   In the Implicit Grant scenario, it returns access tokens and metadata.
    *   In the Authorization Code Grant scenario, it returns the authentication code and state, if any.


### Variables

In order to use this product you must configure multiple variables.

Add the following block to your `config/variables.yaml` file of our 
main Business Project that will make use of this product. Afterwards
set the values that you collected before.
(Note, that in the Designer these variables can be defined
in any project so there is no need to unpack the demo project).

```
Variables:
  docusign-connector:
    # Integration key from your applications settings in the DocuSign eSignature "Apps and Keys" page.
    integrationKey: ''
    
    # Secret key from your applications settings in the DocuSign eSignature "Apps and Keys" page.
    # [password]
    secretKey: ''
    
    # If set, use a specific account id, otherwise use the default account of the user. (Probably only makes sense for JWT Token grant.)
    accountId: ''

    # Scope of grant.
    scope: signature impersonation
    
    # Docusign base url for authentication.
    baseUri: https://account-d.docusign.com/oauth

    jwt:
      # If 'true' JWT token grant else user grant (default).
      use: false

      # User ID from your eSignature "Apps and Keys" page.
      userId: ''
    
      # Name of the key file from your applications settings in the DocuSign eSignature "Apps and Keys" page relative to the "configuration" directory.
      keyFile: 'docusign.pem'
    # This property provides a call back that after the signer completes or ends the signing ceremony, DocuSign redirects the user's browser back to your app via the returnUrl that you supplied in the request.
    returnPage: 'http://localhost:8081/'

    # This property is a string array which must include your site’s URL along with https://apps-d.docusign.com/send/ - opens in new window if your app is in the demo environment or https://apps.docusign.com - opens in new window if it is in production. Your domain must have a valid SSL certificate (such as https://my.site.com) for embedding in production environments. You can use http://localhost for development and testing.
    frameAncestors: 'http://localhost:8081/, https://apps-d.docusign.com'
    
    # This property must include https://apps-d.docusign.com/send/ - opens in new window if your app is in the demo environment or https://apps.docusign.com - opens in new window if it is in production.
    messageOrigins: 'https://apps-d.docusign.com'

```

### Optional: Allow System Authentication (JWT)

The Demo process contains a final service part, where the Axon Ivy platform acts in the name of a user.  
![docusign-props](images/systemDrivenProcess.png)

This interaction requires a JSON Web Token (JWT) authentication setup:

1. Edit the DocuSign `application` as in step 3. of the general setup.
2. In the section `Authentication` click on `Generate RSA` in order to create a secure key-pair.  
 ![docusign-gen-rsa](images/authenticationGenerateRSA.png)

3. Store the generated private key:
	1. Copy the generated 'Private Key' to your clipboard.
	2. Save the changed application settings.
	3. Create a new empty text file called `docusign.pem` in your Designer 'configuration' directory.
	4. Paste the contents of your clipboard into the `docusign.pem` file.
	5. You can use another storage location for the pem file. Adjust the variable: `docusign-connector.jwt.keyFile` to refer to it. It should be a relative path to the 'configuration' directory or an absolute path on your system.  
![docusign-pem](images/docuSignPem.png)

4. Define a user to act as a service account:
	1. Navigate to the `Users` overview and select your preferred service user.
	2. Copy the `API Username (id)` stated on the user detail page.
	3. Set it into the variable `docusign-connector.jwt.userId`.
	
5. JWT will be used automatically for processes run by the system user. If you want to use it
   in general, set variable `docusign-connector.jwt.use` to `true`.

6. Done. Start a signing process. Once all recipients have signed a document, the system service interaction will attach the signed document to the origin Case.
