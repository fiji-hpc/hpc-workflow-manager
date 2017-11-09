package cz.it4i.fiji.haas_java_client.proxy;

public class UserAndLimitationManagementWsSoapProxy implements cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap {
  private String _endpoint = null;
  private cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap userAndLimitationManagementWsSoap = null;
  
  public UserAndLimitationManagementWsSoapProxy() {
    _initUserAndLimitationManagementWsSoapProxy();
  }
  
  public UserAndLimitationManagementWsSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initUserAndLimitationManagementWsSoapProxy();
  }
  
  private void _initUserAndLimitationManagementWsSoapProxy() {
    try {
      userAndLimitationManagementWsSoap = (new cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsLocator()).getUserAndLimitationManagementWsSoap();
      if (userAndLimitationManagementWsSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)userAndLimitationManagementWsSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)userAndLimitationManagementWsSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (userAndLimitationManagementWsSoap != null)
      ((javax.xml.rpc.Stub)userAndLimitationManagementWsSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap getUserAndLimitationManagementWsSoap() {
    if (userAndLimitationManagementWsSoap == null)
      _initUserAndLimitationManagementWsSoapProxy();
    return userAndLimitationManagementWsSoap;
  }
  
  public java.lang.String authenticateUserPassword(cz.it4i.fiji.haas_java_client.proxy.PasswordCredentialsExt credentials) throws java.rmi.RemoteException{
    if (userAndLimitationManagementWsSoap == null)
      _initUserAndLimitationManagementWsSoapProxy();
    return userAndLimitationManagementWsSoap.authenticateUserPassword(credentials);
  }
  
  public java.lang.String authenticateUserDigitalSignature(cz.it4i.fiji.haas_java_client.proxy.DigitalSignatureCredentialsExt credentials) throws java.rmi.RemoteException{
    if (userAndLimitationManagementWsSoap == null)
      _initUserAndLimitationManagementWsSoapProxy();
    return userAndLimitationManagementWsSoap.authenticateUserDigitalSignature(credentials);
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.ResourceUsageExt[] getCurrentUsageAndLimitationsForCurrentUser(java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (userAndLimitationManagementWsSoap == null)
      _initUserAndLimitationManagementWsSoapProxy();
    return userAndLimitationManagementWsSoap.getCurrentUsageAndLimitationsForCurrentUser(sessionCode);
  }
  
  
}