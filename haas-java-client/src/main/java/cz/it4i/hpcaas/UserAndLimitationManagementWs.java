

/**
 * UserAndLimitationManagementWs.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.7.6  Built on : Jul 30, 2017 (09:08:31 BST)
 */

    package cz.it4i.hpcaas;

    /*
     *  UserAndLimitationManagementWs java interface
     */

    public interface UserAndLimitationManagementWs {
          

        /**
          * Auto generated method signature
          * 
                    * @param authenticateUserDigitalSignature0
                
         */

         
                     public cz.it4i.hpcaas.AuthenticateUserDigitalSignatureResponse authenticateUserDigitalSignature(

                        cz.it4i.hpcaas.AuthenticateUserDigitalSignature authenticateUserDigitalSignature0)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param authenticateUserDigitalSignature0
            
          */
        public void startauthenticateUserDigitalSignature(

            cz.it4i.hpcaas.AuthenticateUserDigitalSignature authenticateUserDigitalSignature0,

            final cz.it4i.hpcaas.UserAndLimitationManagementWsCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getCurrentUsageAndLimitationsForCurrentUser2
                
         */

         
                     public cz.it4i.hpcaas.GetCurrentUsageAndLimitationsForCurrentUserResponse getCurrentUsageAndLimitationsForCurrentUser(

                        cz.it4i.hpcaas.GetCurrentUsageAndLimitationsForCurrentUser getCurrentUsageAndLimitationsForCurrentUser2)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getCurrentUsageAndLimitationsForCurrentUser2
            
          */
        public void startgetCurrentUsageAndLimitationsForCurrentUser(

            cz.it4i.hpcaas.GetCurrentUsageAndLimitationsForCurrentUser getCurrentUsageAndLimitationsForCurrentUser2,

            final cz.it4i.hpcaas.UserAndLimitationManagementWsCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param authenticateUserPassword4
                
         */

         
                     public cz.it4i.hpcaas.AuthenticateUserPasswordResponse authenticateUserPassword(

                        cz.it4i.hpcaas.AuthenticateUserPassword authenticateUserPassword4)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param authenticateUserPassword4
            
          */
        public void startauthenticateUserPassword(

            cz.it4i.hpcaas.AuthenticateUserPassword authenticateUserPassword4,

            final cz.it4i.hpcaas.UserAndLimitationManagementWsCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    