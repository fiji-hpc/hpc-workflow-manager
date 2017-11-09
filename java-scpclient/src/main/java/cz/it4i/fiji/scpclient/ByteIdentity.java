package cz.it4i.fiji.scpclient;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

class ByteIdentity  implements Identity{

	private KeyPair keyPair;

	public ByteIdentity(JSch jsch,byte []prvKey) throws JSchException {
		this.keyPair = KeyPair.load(jsch, prvKey, null);
	}

	@Override
	public boolean setPassphrase(byte[] passphrase) throws JSchException {
		return keyPair.decrypt(passphrase);
	}

	@Override
	public byte[] getPublicKeyBlob() {
		return keyPair.getPublicKeyBlob();
	}

	@Override
	public byte[] getSignature(byte[] data) {
		return keyPair.getSignature(data);
	}

	@Override
	public boolean decrypt() {
		return false;
	}

	@Override
	public String getAlgName() {
		if(keyPair.getKeyType() == KeyPair.RSA) {
			return "ssh-rsa";
		} else if(keyPair.getKeyType() == KeyPair.DSA) {
			return "ssh-dsa";
		}
		throw new UnsupportedOperationException("Key type:" + keyPair.getKeyType() + " not supported.");
	}

	@Override
	public String getName() {
		return keyPair.getPublicKeyComment();
	}

	@Override
	public boolean isEncrypted() {
		return keyPair.isEncrypted();
	}

	@Override
	public void clear() {
		keyPair.dispose();
		keyPair = null;
	}

}
