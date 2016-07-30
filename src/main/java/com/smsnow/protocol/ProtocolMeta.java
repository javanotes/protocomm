package com.smsnow.protocol;

import java.util.TreeMap;

import org.springframework.util.Assert;

import java.util.Map.Entry;

public class ProtocolMeta {

	private final TreeMap<Integer, FormatMeta> formats = new TreeMap<>();
	private final String name;
	public ProtocolMeta(String name)
	{
		this.name = name;
	}
	public void add(FormatMeta fm)
	{
		getFormats().put(fm.offset, fm);
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	protected void validate() {
		int off = 0, len = 0;
		setSize(len);
		for(Entry<Integer, FormatMeta> e : getFormats().entrySet())
		{
			FormatMeta fm = e.getValue();
			Assert.isTrue(fm.offset == (off + len), name+" => Incorrect length at offset:"+off);
			off = fm.offset;
			len = fm.length;
			size += len;
						
		}
		
	}
	public TreeMap<Integer, FormatMeta> getFormats() {
		return formats;
	}
	private int size;
}
