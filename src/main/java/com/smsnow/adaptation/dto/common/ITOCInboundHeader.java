/* Generated by SMSNOW protocol CodeGen on Wed Aug 03 12:48:40 IST 2016. 

0 4 Binary totalMessageLenLLLL
4 2 Binary itocHeaderLenLL
6 2 Binary itocHeaderPrefixZZ
8 8 Text ITOCExitName ZMXITOC0
16 8 Text ReservedFiller1
24 8 Text ClientID
32 8 Text ReservedFiller2
40 8 Text DATASTOREID
48 8 Text RACFUserID
56 8 Text RACFGroupName
64 8 Text IOPCBLTERMOverride 
72 8 Text IOPCBModnameOverride 
80 3 Text ITOCTrace
83 1 Text CommitMode 0
84 1 Text SecurityScope N
85 1 Text MessageType
86 1 Text MessageTimerValue
87 5 Text Filler
92 8 Text TPIPEName
100 4 Text SendSequenceNumber
*/
package com.smsnow.adaptation.dto.common;

import com.smsnow.adaptation.protocol.Protocol;
import java.io.Serializable;
import com.smsnow.adaptation.protocol.Format;
import com.smsnow.adaptation.protocol.Attribute;

@Protocol(name = "ITOCINBOUNDHEADER")
public class ITOCInboundHeader implements Serializable {

	private static final long serialVersionUID = 1L;
	@Format(attribute = Attribute.TEXT, offset = 24, length = 8, constant = "")
	private String clientID;
	@Format(attribute = Attribute.BINARY, offset = 6, length = 2, constant = "")
	private short itocHeaderPrefixZZ;
	@Format(attribute = Attribute.BINARY, offset = 4, length = 2, constant = "")
	private short itocHeaderLenLL;
	@Format(attribute = Attribute.TEXT, offset = 84, length = 1, constant = "N")
	private String securityScope;
	@Format(attribute = Attribute.TEXT, offset = 87, length = 5, constant = "")
	private String filler;
	@Format(attribute = Attribute.TEXT, offset = 64, length = 8, constant = "")
	private String iOPCBLTERMOverride;
	@Format(attribute = Attribute.TEXT, offset = 72, length = 8, constant = "")
	private String iOPCBModnameOverride;
	@Format(attribute = Attribute.TEXT, offset = 40, length = 8, constant = "")
	private String dATASTOREID;
	@Format(attribute = Attribute.TEXT, offset = 8, length = 8, constant = "ZMXITOC0")
	private String iTOCExitName;
	@Format(attribute = Attribute.TEXT, offset = 16, length = 8, constant = "")
	private String reservedFiller1;
	@Format(attribute = Attribute.TEXT, offset = 86, length = 1, constant = "")
	private String messageTimerValue;
	@Format(attribute = Attribute.TEXT, offset = 32, length = 8, constant = "")
	private String reservedFiller2;
	@Format(attribute = Attribute.TEXT, offset = 80, length = 3, constant = "")
	private String iTOCTrace;
	@Format(attribute = Attribute.TEXT, offset = 48, length = 8, constant = "")
	private String rACFUserID;
	@Format(attribute = Attribute.TEXT, offset = 92, length = 8, constant = "")
	private String tPIPEName;
	@Format(attribute = Attribute.TEXT, offset = 100, length = 4, constant = "")
	private String sendSequenceNumber;
	@Format(attribute = Attribute.TEXT, offset = 85, length = 1, constant = "")
	private String messageType;
	@Format(attribute = Attribute.TEXT, offset = 83, length = 1, constant = "0")
	private String commitMode;
	@Format(attribute = Attribute.TEXT, offset = 56, length = 8, constant = "")
	private String rACFGroupName;
	@Format(attribute = Attribute.BINARY, offset = 0, length = 4, constant = "")
	private int totalMessageLenLLLL;

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public short getItocHeaderPrefixZZ() {
		return itocHeaderPrefixZZ;
	}

	public void setItocHeaderPrefixZZ(short itocHeaderPrefixZZ) {
		this.itocHeaderPrefixZZ = itocHeaderPrefixZZ;
	}

	public short getItocHeaderLenLL() {
		return itocHeaderLenLL;
	}

	public void setItocHeaderLenLL(short itocHeaderLenLL) {
		this.itocHeaderLenLL = itocHeaderLenLL;
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

	public String getIOPCBModnameOverride() {
		return iOPCBModnameOverride;
	}

	public void setIOPCBModnameOverride(String iOPCBModnameOverride) {
		this.iOPCBModnameOverride = iOPCBModnameOverride;
	}

	public String getDATASTOREID() {
		return dATASTOREID;
	}

	public void setDATASTOREID(String dATASTOREID) {
		this.dATASTOREID = dATASTOREID;
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

	public String getMessageTimerValue() {
		return messageTimerValue;
	}

	public void setMessageTimerValue(String messageTimerValue) {
		this.messageTimerValue = messageTimerValue;
	}

	public String getReservedFiller2() {
		return reservedFiller2;
	}

	public void setReservedFiller2(String reservedFiller2) {
		this.reservedFiller2 = reservedFiller2;
	}

	public String getITOCTrace() {
		return iTOCTrace;
	}

	public void setITOCTrace(String iTOCTrace) {
		this.iTOCTrace = iTOCTrace;
	}

	public String getRACFUserID() {
		return rACFUserID;
	}

	public void setRACFUserID(String rACFUserID) {
		this.rACFUserID = rACFUserID;
	}

	public String getTPIPEName() {
		return tPIPEName;
	}

	public void setTPIPEName(String tPIPEName) {
		this.tPIPEName = tPIPEName;
	}

	public String getSendSequenceNumber() {
		return sendSequenceNumber;
	}

	public void setSendSequenceNumber(String sendSequenceNumber) {
		this.sendSequenceNumber = sendSequenceNumber;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getCommitMode() {
		return commitMode;
	}

	public void setCommitMode(String commitMode) {
		this.commitMode = commitMode;
	}

	public String getRACFGroupName() {
		return rACFGroupName;
	}

	public void setRACFGroupName(String rACFGroupName) {
		this.rACFGroupName = rACFGroupName;
	}

	public int getTotalMessageLenLLLL() {
		return totalMessageLenLLLL;
	}

	public void setTotalMessageLenLLLL(int totalMessageLenLLLL) {
		this.totalMessageLenLLLL = totalMessageLenLLLL;
	}

	public ITOCInboundHeader() {
	}
}