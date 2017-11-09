package cz.it4i.fiji.haas_java_client.proxy;

public class JobManagementWsSoapProxy implements cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap {
  private String _endpoint = null;
  private cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap jobManagementWsSoap = null;
  
  public JobManagementWsSoapProxy() {
    _initJobManagementWsSoapProxy();
  }
  
  public JobManagementWsSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initJobManagementWsSoapProxy();
  }
  
  private void _initJobManagementWsSoapProxy() {
    try {
      jobManagementWsSoap = (new cz.it4i.fiji.haas_java_client.proxy.JobManagementWsLocator()).getJobManagementWsSoap();
      if (jobManagementWsSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)jobManagementWsSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)jobManagementWsSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (jobManagementWsSoap != null)
      ((javax.xml.rpc.Stub)jobManagementWsSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap getJobManagementWsSoap() {
    if (jobManagementWsSoap == null)
      _initJobManagementWsSoapProxy();
    return jobManagementWsSoap;
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt createJob(cz.it4i.fiji.haas_java_client.proxy.JobSpecificationExt specification, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (jobManagementWsSoap == null)
      _initJobManagementWsSoapProxy();
    return jobManagementWsSoap.createJob(specification, sessionCode);
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt submitJob(long createdJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (jobManagementWsSoap == null)
      _initJobManagementWsSoapProxy();
    return jobManagementWsSoap.submitJob(createdJobInfoId, sessionCode);
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt cancelJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (jobManagementWsSoap == null)
      _initJobManagementWsSoapProxy();
    return jobManagementWsSoap.cancelJob(submittedJobInfoId, sessionCode);
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt[] listJobsForCurrentUser(java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (jobManagementWsSoap == null)
      _initJobManagementWsSoapProxy();
    return jobManagementWsSoap.listJobsForCurrentUser(sessionCode);
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt getCurrentInfoForJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (jobManagementWsSoap == null)
      _initJobManagementWsSoapProxy();
    return jobManagementWsSoap.getCurrentInfoForJob(submittedJobInfoId, sessionCode);
  }
  
  
}