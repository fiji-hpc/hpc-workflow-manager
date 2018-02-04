package cz.it4i.fiji.haas_java_client.proxy;

public class DataTransferWsSoapProxy implements cz.it4i.fiji.haas_java_client.proxy.DataTransferWsSoap {
  private String _endpoint = null;
  private cz.it4i.fiji.haas_java_client.proxy.DataTransferWsSoap dataTransferWsSoap = null;
  
  public DataTransferWsSoapProxy() {
    _initDataTransferWsSoapProxy();
  }
  
  public DataTransferWsSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initDataTransferWsSoapProxy();
  }
  
  private void _initDataTransferWsSoapProxy() {
    try {
      dataTransferWsSoap = (new cz.it4i.fiji.haas_java_client.proxy.DataTransferWsLocator()).getDataTransferWsSoap();
      if (dataTransferWsSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)dataTransferWsSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)dataTransferWsSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (dataTransferWsSoap != null)
      ((javax.xml.rpc.Stub)dataTransferWsSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.DataTransferWsSoap getDataTransferWsSoap() {
    if (dataTransferWsSoap == null)
      _initDataTransferWsSoapProxy();
    return dataTransferWsSoap;
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.DataTransferMethodExt getDataTransferMethod(byte[] ipAddress, int port, long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (dataTransferWsSoap == null)
      _initDataTransferWsSoapProxy();
    return dataTransferWsSoap.getDataTransferMethod(ipAddress, port, submittedJobInfoId, sessionCode);
  }
  
  public void endDataTransfer(cz.it4i.fiji.haas_java_client.proxy.DataTransferMethodExt usedTransferMethod, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (dataTransferWsSoap == null)
      _initDataTransferWsSoapProxy();
    dataTransferWsSoap.endDataTransfer(usedTransferMethod, sessionCode);
  }
  
  public void sendDataToJob(byte[] data, long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (dataTransferWsSoap == null)
      _initDataTransferWsSoapProxy();
    dataTransferWsSoap.sendDataToJob(data, submittedJobInfoId, sessionCode);
  }
  
  public java.lang.String readDataFromJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (dataTransferWsSoap == null)
      _initDataTransferWsSoapProxy();
    return dataTransferWsSoap.readDataFromJob(submittedJobInfoId, sessionCode);
  }
  
  
}