/**
 * JobManagementWsSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cz.it4i.fiji.haas_java_client.proxy;

public interface JobManagementWsSoap extends java.rmi.Remote {
    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt createJob(cz.it4i.fiji.haas_java_client.proxy.JobSpecificationExt specification, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt submitJob(long createdJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt cancelJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public void deleteJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException;
    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt[] listJobsForCurrentUser(java.lang.String sessionCode) throws java.rmi.RemoteException;
    public cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt getCurrentInfoForJob(long submittedJobInfoId, java.lang.String sessionCode) throws java.rmi.RemoteException;
}
