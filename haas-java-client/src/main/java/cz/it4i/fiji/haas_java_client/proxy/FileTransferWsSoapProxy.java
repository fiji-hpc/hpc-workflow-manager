package cz.it4i.fiji.haas_java_client.proxy;

public class FileTransferWsSoapProxy implements cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap {
  private String _endpoint = null;
  private cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap fileTransferWsSoap = null;
  
  public FileTransferWsSoapProxy() {
    _initFileTransferWsSoapProxy();
  }
  
  public FileTransferWsSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initFileTransferWsSoapProxy();
  }
  
  private void _initFileTransferWsSoapProxy() {
    try {
      fileTransferWsSoap = (new cz.it4i.fiji.haas_java_client.proxy.FileTransferWsLocator()).getFileTransferWsSoap();
      if (fileTransferWsSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)fileTransferWsSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)fileTransferWsSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (fileTransferWsSoap != null)
      ((javax.xml.rpc.Stub)fileTransferWsSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap getFileTransferWsSoap() {
    if (fileTransferWsSoap == null)
      _initFileTransferWsSoapProxy();
    return fileTransferWsSoap;
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.FileTransferMethodExt getFileTransferMethod(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (fileTransferWsSoap == null)
      _initFileTransferWsSoapProxy();
    return fileTransferWsSoap.getFileTransferMethod(submittedJobInfoId, sessionCode);
  }
  
  public void endFileTransfer(long submittedJobInfoId, cz.it4i.fiji.haas_java_client.proxy.FileTransferMethodExt usedTransferMethod, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (fileTransferWsSoap == null)
      _initFileTransferWsSoapProxy();
    fileTransferWsSoap.endFileTransfer(submittedJobInfoId, usedTransferMethod, sessionCode);
  }
  
  public cz.it4i.fiji.haas_java_client.proxy.JobFileContentExt[] downloadPartsOfJobFilesFromCluster(long submittedJobInfoId, cz.it4i.fiji.haas_java_client.proxy.TaskFileOffsetExt[] taskFileOffsets, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (fileTransferWsSoap == null)
      _initFileTransferWsSoapProxy();
    return fileTransferWsSoap.downloadPartsOfJobFilesFromCluster(submittedJobInfoId, taskFileOffsets, sessionCode);
  }
  
  public java.lang.String[] listChangedFilesForJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (fileTransferWsSoap == null)
      _initFileTransferWsSoapProxy();
    return fileTransferWsSoap.listChangedFilesForJob(submittedJobInfoId, sessionCode);
  }
  
  public byte[] downloadFileFromCluster(long submittedJobInfoId, java.lang.String relativeFilePath, java.lang.String sessionCode) throws java.rmi.RemoteException{
    if (fileTransferWsSoap == null)
      _initFileTransferWsSoapProxy();
    return fileTransferWsSoap.downloadFileFromCluster(submittedJobInfoId, relativeFilePath, sessionCode);
  }
  
  
}