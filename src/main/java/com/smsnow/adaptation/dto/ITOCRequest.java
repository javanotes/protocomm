/* Generated by SMSNOW protocol CodeGen on Wed Aug 03 17:47:53 IST 2016. 

0 108 inheader MVSInboundITOCHeader
108 304 appheader ApplicationHeader
*/
package com.smsnow.adaptation.dto;

import com.smsnow.adaptation.protocol.Protocol;
import java.io.Serializable;
import com.smsnow.adaptation.dto.ApplicationHeader;
import com.smsnow.adaptation.protocol.Format;
import com.smsnow.adaptation.protocol.Attribute;
import com.smsnow.adaptation.dto.ITOCInboundHeader;

@Protocol(name = "ITOCREQUEST")
public class ITOCRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	@Format(attribute = Attribute.APPHEADER, offset = 108, length = 304, constant = "")
	private ApplicationHeader applicationHeader = null;
	@Format(attribute = Attribute.INHEADER, offset = 0, length = 108, constant = "")
	private ITOCInboundHeader mVSInboundITOCHeader = null;

	public ApplicationHeader getApplicationHeader() {
		return applicationHeader;
	}

	public void setApplicationHeader(ApplicationHeader applicationHeader) {
		this.applicationHeader = applicationHeader;
	}

	public ITOCInboundHeader getMVSInboundITOCHeader() {
		return mVSInboundITOCHeader;
	}

	public void setMVSInboundITOCHeader(ITOCInboundHeader mVSInboundITOCHeader) {
		this.mVSInboundITOCHeader = mVSInboundITOCHeader;
	}

	public ITOCRequest() {
		applicationHeader = new ApplicationHeader();
		mVSInboundITOCHeader = new ITOCInboundHeader();
	}
}