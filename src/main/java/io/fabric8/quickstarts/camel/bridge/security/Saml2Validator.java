package io.fabric8.quickstarts.camel.bridge.security;

import java.util.List;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.validate.Credential;
import org.apache.wss4j.dom.validate.SamlAssertionValidator;
import org.opensaml.core.xml.schema.impl.XSStringImpl;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;

public class Saml2Validator extends SamlAssertionValidator {

    @Override
    public Credential validate(Credential credential, RequestData data) throws WSSecurityException {
        //validate token username/roles we fetched from RH SSO 
        Credential validatedCredential = super.validate(credential, data);
        SamlAssertionWrapper assertion = validatedCredential.getSamlAssertion();
        
        if (!"admin".equals(assertion.getSaml2().getSubject().getNameID().getValue())) {
            //validate the username
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity");
        }
        if (!"keycloak".equals(assertion.getIssuerString())) {
            //validate the Saml2 token issuer
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity");
        }

        Assertion saml2Assertion = assertion.getSaml2();
        
        if (saml2Assertion == null) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity");
        } 
        List<AttributeStatement> attributeStatements = saml2Assertion.getAttributeStatements();
        if (attributeStatements == null || attributeStatements.isEmpty()) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity");
        } else {
            //validate roles associated with the user
            XSStringImpl role = (XSStringImpl)attributeStatements.get(0).getAttributes().get(0).getAttributeValues().get(0);
            if (!"uma_authorization".contentEquals(role.getValue())) {
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity");
            }
            role = (XSStringImpl)attributeStatements.get(0).getAttributes().get(0).getAttributeValues().get(1);
            if (!"offline_access".contentEquals(role.getValue())) {
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "invalidSAMLsecurity");
            }
        }
        
        return validatedCredential;
    }

}
