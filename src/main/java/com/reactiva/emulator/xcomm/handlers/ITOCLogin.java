package com.reactiva.emulator.xcomm.handlers;


import com.smsnow.protocol.Protocol;
import com.smsnow.protocol.IType;
import java.io.Serializable;
import com.smsnow.protocol.Format;
import com.smsnow.protocol.Attribute;

@Protocol(name = "null")
public class ITOCLogin implements IType, Serializable {

	private static final long serialVersionUID = 1L;
	@Format(attribute = Attribute.TEXT, offset = 420, length = 8, constant = "")
	private String userPassword;
	@Format(attribute = Attribute.UNDEF, offset = 108, length = 304, constant = "")
	private byte[] applicationHeader;
	@Format(attribute = Attribute.TEXT, offset = 428, length = 8, constant = "")
	private String oldPassword;
	@Format(attribute = Attribute.UNDEF, offset = 0, length = 108, constant = "")
	private byte[] mVSInboundITOCHeader;
	@Format(attribute = Attribute.TEXT, offset = 436, length = 1, constant = "")
	private String action;
	@Format(attribute = Attribute.UNDEF, offset = 437, length = 4, constant = "")
	private byte[] iTOCTrailer;
	@Format(attribute = Attribute.TEXT, offset = 412, length = 8, constant = "")
	private String userLogonID;

	@Override
	public String toString() {
		return "ITOCLogin [userPassword=" + userPassword + ", oldPassword=" + oldPassword + ", action=" + action
				+ ", userLogonID=" + userLogonID + "]";
	}

	public ITOCLogin(String userPassword, String oldPassword, String action, String userLogonID) {
		super();
		this.userPassword = userPassword;
		this.oldPassword = oldPassword;
		this.action = action;
		this.userLogonID = userLogonID;
	}

	public short code() {
		return 1;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public byte[] getApplicationHeader() {
		return applicationHeader;
	}

	public void setApplicationHeader(byte[] applicationHeader) {
		this.applicationHeader = applicationHeader;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public byte[] getMVSInboundITOCHeader() {
		return mVSInboundITOCHeader;
	}

	public void setMVSInboundITOCHeader(byte[] mVSInboundITOCHeader) {
		this.mVSInboundITOCHeader = mVSInboundITOCHeader;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public byte[] getITOCTrailer() {
		return iTOCTrailer;
	}

	public void setITOCTrailer(byte[] iTOCTrailer) {
		this.iTOCTrailer = iTOCTrailer;
	}

	public String getUserLogonID() {
		return userLogonID;
	}

	public void setUserLogonID(String userLogonID) {
		this.userLogonID = userLogonID;
	}

	public ITOCLogin() {
	}
}