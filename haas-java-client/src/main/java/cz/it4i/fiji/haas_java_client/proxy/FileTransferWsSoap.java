/**
 * FileTransferWsSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public interface FileTransferWsSoap extends java.rmi.Remote {
    public cz.it4i.fiji.haas_java_client.proxy.FileTransferMethodExt getFileTransferMethod(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public void endFileTransfer(long submittedJobInfoId, cz.it4i.fiji.haas_java_client.proxy.FileTransferMethodExt usedTransferMethod, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public cz.it4i.fiji.haas_java_client.proxy.JobFileContentExt[] downloadPartsOfJobFilesFromCluster(long submittedJobInfoId, cz.it4i.fiji.haas_java_client.proxy.TaskFileOffsetExt[] taskFileOffsets, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public java.lang.String[] listChangedFilesForJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public byte[] downloadFileFromCluster(long submittedJobInfoId, java.lang.String relativeFilePath, java.lang.String sessionCode) throws java.rmi.RemoteException;
}
