/**
 * DataTransferWsSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public interface DataTransferWsSoap extends java.rmi.Remote {
    public cz.it4i.fiji.haas_java_client.proxy.DataTransferMethodExt getDataTransferMethod(byte[] ipAddress, int port, long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public void endDataTransfer(cz.it4i.fiji.haas_java_client.proxy.DataTransferMethodExt usedTransferMethod, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public void sendDataToJob(byte[] data, long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public java.lang.String readDataFromJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException;
}
