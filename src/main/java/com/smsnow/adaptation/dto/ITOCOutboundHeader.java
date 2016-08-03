/* Generated by SMSNOW protocol CodeGen on Wed Aug 03 16:14:42 IST 2016. 

0 4 Binary totalMessageLenLLLL
4 2 Binary itocHeaderLenLL
6 2 Binary itocHeaderPrefixZZ
8 8 Text ITOCExitName ZMXITOC0
16 8 Text ReservedFiller1 Zeroes
24 8 Text ClientID
32 8 Text ReservedFiller2 blanks
40 8 Text DATASTOREID
48 8 Text RACFID
56 8 Text RACFGroupID
64 8 Text IOPCBLTERMOverride
72 8 Text IOPCBModName
80 3 Text TraceOption
83 1 Text CommitMode
84 1 Text SecurityScope
85 1 Text MessageType
86 6 Text Filler
92 8 Text TPIPEName
100 4 Text SendSequenceNumber
104 4 Binary ReturnCode
108 4 Binary ReasonCode
112 4 Binary SenseCode
116 8 Text OutputModname
124 30 Text ErrorMessageText
*/
package com.smsnow.adaptation.dto;

import com.smsnow.adaptation.protocol.Protocol;
import java.io.Serializable;
import com.smsnow.adaptation.protocol.Format;
import com.smsnow.adaptation.protocol.Attribute;

@Protocol(name = "ITOCOUTBOUNDHEADER")
public class ITOCOutboundHeader implements Serializable {

	private static final long serialVersionUID = 1L;
	@Format(attribute = Attribute.TEXT, offset = 24, length = 8, constant = "")
	private String clientID = "";
	@Format(attribute = Attribute.BINARY, offset = 6, length = 2, constant = "")
	private byte[] itocHeaderPrefixZZ = new byte[2];
	@Format(attribute = Attribute.BINARY, offset = 4, length = 2, constant = "")
	private byte[] itocHeaderLenLL = new byte[2];
	@Format(attribute = Attribute.TEXT, offset = 80, length = 3, constant = "")
	private String traceOption = "";
	@Format(attribute = Attribute.TEXT, offset = 84, length = 1, constant = "")
	private String securityScope = "";
	@Format(attribute = Attribute.TEXT, offset = 86, length = 6, constant = "")
	private String filler = "";
	@Format(attribute = Attribute.TEXT, offset = 64, length = 8, constant = "")
	private String iOPCBLTERMOverride = "";
	@Format(attribute = Attribute.TEXT, offset = 40, length = 8, constant = "")
	private String dATASTOREID = "";
	@Format(attribute = Attribute.TEXT, offset = 56, length = 8, constant = "")
	private String rACFGroupID = "";
	@Format(attribute = Attribute.TEXT, offset = 72, length = 8, constant = "")
	private String iOPCBModName = "";
	@Format(attribute = Attribute.TEXT, offset = 8, length = 8, constant = "ZMXITOC0")
	private String iTOCExitName = "";
	@Format(attribute = Attribute.TEXT, offset = 16, length = 8, constant = "Zeroes")
	private String reservedFiller1 = "";
	@Format(attribute = Attribute.TEXT, offset = 32, length = 8, constant = "blanks")
	private String reservedFiller2 = "";
	@Format(attribute = Attribute.TEXT, offset = 92, length = 8, constant = "")
	private String tPIPEName = "";
	@Format(attribute = Attribute.BINARY, offset = 104, length = 4, constant = "")
	private byte[] returnCode = new byte[4];
	@Format(attribute = Attribute.TEXT, offset = 100, length = 4, constant = "")
	private String sendSequenceNumber = "";
	@Format(attribute = Attribute.BINARY, offset = 112, length = 4, constant = "")
	private byte[] senseCode = new byte[4];
	@Format(attribute = Attribute.TEXT, offset = 85, length = 1, constant = "")
	private String messageType = "";
	@Format(attribute = Attribute.TEXT, offset = 48, length = 8, constant = "")
	private String rACFID = "";
	@Format(attribute = Attribute.TEXT, offset = 83, length = 1, constant = "")
	private String commitMode = "";
	@Format(attribute = Attribute.BINARY, offset = 108, length = 4, constant = "")
	private byte[] reasonCode = new byte[4];
	@Format(attribute = Attribute.TEXT, offset = 116, length = 8, constant = "")
	private String outputModname = "";
	@Format(attribute = Attribute.TEXT, offset = 124, length = 30, constant = "")
	private String errorMessageText = "";
	@Format(attribute = Attribute.BINARY, offset = 0, length = 4, constant = "")
	private byte[] totalMessageLenLLLL = new byte[4];

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public byte[] getItocHeaderPrefixZZ() {
		return itocHeaderPrefixZZ;
	}

	public void setItocHeaderPrefixZZ(byte[] itocHeaderPrefixZZ) {
		this.itocHeaderPrefixZZ = itocHeaderPrefixZZ;
	}

	public byte[] getItocHeaderLenLL() {
		return itocHeaderLenLL;
	}

	public void setItocHeaderLenLL(byte[] itocHeaderLenLL) {
		this.itocHeaderLenLL = itocHeaderLenLL;
	}

	public String getTraceOption() {
		return traceOption;
	}

	public void setTraceOption(String traceOption) {
		this.traceOption = traceOption;
	}

	public String getSecurityScope() {
		return securityScope;
	}

	public void setSecurityScope(String securityScope) {
		this.securityScope = securityScope;
	}

	public String getFiller() {
		return filler;
	}

	public void setFiller(String filler) {
		this.filler = filler;
	}

	public String getIOPCBLTERMOverride() {
		return iOPCBLTERMOverride;
	}

	public void setIOPCBLTERMOverride(String iOPCBLTERMOverride) {
		this.iOPCBLTERMOverride = iOPCBLTERMOverride;
	}

	public String getDATASTOREID() {
		return dATASTOREID;
	}

	public void setDATASTOREID(String dATASTOREID) {
		this.dATASTOREID = dATASTOREID;
	}

	public String getRACFGroupID() {
		return rACFGroupID;
	}

	public void setRACFGroupID(String rACFGroupID) {
		this.rACFGroupID = rACFGroupID;
	}

	public String getIOPCBModName() {
		return iOPCBModName;
	}

	public void setIOPCBModName(String iOPCBModName) {
		this.iOPCBModName = iOPCBModName;
	}

	public String getITOCExitName() {
		return iTOCExitName;
	}

	public void setITOCExitName(String iTOCExitName) {
		this.iTOCExitName = iTOCExitName;
	}

	public String getReservedFiller1() {
		return reservedFiller1;
	}

	public void setReservedFiller1(String reservedFiller1) {
		this.reservedFiller1 = reservedFiller1;
	}

	public String getReservedFiller2() {
		return reservedFiller2;
	}

	public void setReservedFiller2(String reservedFiller2) {
		this.reservedFiller2 = reservedFiller2;
	}

	public String getTPIPEName() {
		return tPIPEName;
	}

	public void setTPIPEName(String tPIPEName) {
		this.tPIPEName = tPIPEName;
	}

	public byte[] getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(byte[] returnCode) {
		this.returnCode = returnCode;
	}

	public String getSendSequenceNumber() {
		return sendSequenceNumber;
	}

	public void setSendSequenceNumber(String sendSequenceNumber) {
		this.sendSequenceNumber = sendSequenceNumber;
	}

	public byte[] getSenseCode() {
		return senseCode;
	}

	public void setSenseCode(byte[] senseCode) {
		this.senseCode = senseCode;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getRACFID() {
		return rACFID;
	}

	public void setRACFID(String rACFID) {
		this.rACFID = rACFID;
	}

	public String getCommitMode() {
		return commitMode;
	}

	public void setCommitMode(String commitMode) {
		this.commitMode = commitMode;
	}

	public byte[] getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(byte[] reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getOutputModname() {
		return outputModname;
	}

	public void setOutputModname(String outputModname) {
		this.outputModname = outputModname;
	}

	public String getErrorMessageText() {
		return errorMessageText;
	}

	public void setErrorMessageText(String errorMessageText) {
		this.errorMessageText = errorMessageText;
	}

	public byte[] getTotalMessageLenLLLL() {
		return totalMessageLenLLLL;
	}

	public void setTotalMessageLenLLLL(byte[] totalMessageLenLLLL) {
		this.totalMessageLenLLLL = totalMessageLenLLLL;
	}

	public ITOCOutboundHeader() {
	}
}