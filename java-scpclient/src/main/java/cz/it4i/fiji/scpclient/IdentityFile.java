package cz.it4i.fiji.scpclient;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

class IdentityFile implements Identity {
	private KeyPair kpair;
	private String identity;

	static IdentityFile newInstance(String prvfile, String pubfile, JSch jsch) throws JSchException {
		KeyPair kpair = KeyPair.load(jsch, prvfile, pubfile);
		return new IdentityFile(prvfile, kpair);
	}

	static IdentityFile newInstance(String name, byte[] prvkey, byte[] pubkey, JSch jsch) throws JSchException {
		KeyPair kpair = KeyPair.load(jsch, prvkey, pubkey);
		return new IdentityFile(name, kpair);
	}

	private IdentityFile(String name, KeyPair kpair) {
		this.identity = name;
		this.kpair = kpair;
	}

	/**
	 * Decrypts this identity with the specified pass-phrase.
	 * 
	 * @param passphrase
	 *            the pass-phrase for this identity.
	 * @return <tt>true</tt> if the decryption is succeeded or this identity is not
	 *         cyphered.
	 */
	@Override
	public boolean setPassphrase(byte[] passphrase) throws JSchException {
		return kpair.decrypt(passphrase);
	}

	/**
	 * Returns the public-key blob.
	 * 
	 * @return the public-key blob
	 */
	@Override
	public byte[] getPublicKeyBlob() {
		return kpair.getPublicKeyBlob();
	}

	/**
	 * Signs on data with this identity, and returns the result.
	 * 
	 * @param data
	 *            data to be signed
	 * @return the signature
	 */
	@Override
	public byte[] getSignature(byte[] data) {
		return kpair.getSignature(data);
	}

	/**
	 * @deprecated This method should not be invoked.
	 * @see #setPassphrase(byte[] passphrase)
	 */
	@Deprecated
	@Override
	public boolean decrypt() {
		throw new RuntimeException("not implemented");
	}

	/**
	 * Returns the name of the key algorithm.
	 * 
	 * @return "ssh-rsa" or "ssh-dss"
	 */
	@Override
	public String getAlgName() {
		if (kpair.getKeyType() == KeyPair.RSA) {
			return "ssh-rsa";
		} else if (kpair.getKeyType() == KeyPair.DSA) {
			return "ssh-dsa";
		}
		throw new UnsupportedOperationException("Key type:" + kpair.getKeyType() + " not supported.");
	}

	/**
	 * Returns the name of this identity. It will be useful to identify this object
	 * in the {@link IdentityRepository}.
	 */
	@Override
	public String getName() {
		return identity;
	}

	/**
	 * Returns <tt>true</tt> if this identity is cyphered.
	 * 
	 * @return <tt>true</tt> if this identity is cyphered.
	 */
	@Override
	public boolean isEncrypted() {
		return kpair.isEncrypted();
	}

	/**
	 * Disposes internally allocated data, like byte array for the private key.
	 */
	@Override
	public void clear() {
		kpair.dispose();
		kpair = null;
	}

	/**
	 * Returns an instance of {@link KeyPair} used in this {@link Identity}.
	 * 
	 * @return an instance of {@link KeyPair} used in this {@link Identity}.
	 */
	public KeyPair getKeyPair() {
		return kpair;
	}
}
