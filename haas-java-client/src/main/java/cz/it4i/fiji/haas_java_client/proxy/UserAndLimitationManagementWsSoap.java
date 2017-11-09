/**
 * UserAndLimitationManagementWsSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public interface UserAndLimitationManagementWsSoap extends java.rmi.Remote {
    public java.lang.String authenticateUserPassword(cz.it4i.fiji.haas_java_client.proxy.PasswordCredentialsExt credentials) throws java.rmi.RemoteException;
    public java.lang.String authenticateUserDigitalSignature(cz.it4i.fiji.haas_java_client.proxy.DigitalSignatureCredentialsExt credentials) throws java.rmi.RemoteException;
    public cz.it4i.fiji.haas_java_client.proxy.ResourceUsageExt[] getCurrentUsageAndLimitationsForCurrentUser(java.lang.String sessionCode) throws java.rmi.RemoteException;
}
