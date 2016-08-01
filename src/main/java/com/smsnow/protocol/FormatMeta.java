package com.smsnow.protocol;

import java.lang.reflect.Method;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class FormatMeta {
	/**
	 * 
	 * @param offset
	 * @param length
	 * @param attr
	 */
	FormatMeta(int offset, int length, Attribute attr) {
		super();
		this.offset = offset;
		this.length = length;
		this.attr = attr;
	}
	private volatile boolean introspected = false;
	/**
	 * Use this constructor in dynamic usage.
	 * @param offset
	 * @param length
	 * @param attr
	 * @param fieldName
	 */
	public FormatMeta(int offset, int length, Attribute attr, String fieldName) {
		this(offset, length, attr);
		setFieldName(fieldName);
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	final int offset, length;
	final Attribute attr;
	private String constant;
	private String fieldName;
	boolean isDateFld;
	String dateFormat;
	Method getter, setter;
	public void getter(Method m) {
		getter = m;
	}
	public void setter(Method m) {
		setter = m;
	}
	public String getConstant() {
		return constant;
	}
	public void setConstant(String constant) {
		this.constant = constant;
	}
	
	void introspect(Class<?> protoClassTyp, Class<?>...args) {
		if (!introspected) {
			introspected = true;
			Method m = ClassUtils.getMethodIfAvailable(protoClassTyp, AbstractFixedLenCodec.getter(getFieldName()));
			Assert.notNull(m, getFieldName() + " Expecting a public getter");
			m.setAccessible(true);
			getter(m);
			
			if(args.length > 0)
			{
				m = ClassUtils.getMethodIfAvailable(protoClassTyp, AbstractFixedLenCodec.setter(getFieldName()), args);
				Assert.notNull(m, getFieldName() + " Expecting a public setter");
				m.setAccessible(true);
				setter(m);
			}
			else
			{
				String setter = AbstractFixedLenCodec.setter(getFieldName());
				for(Method m2 : protoClassTyp.getMethods())
				{
					if(m2.getName().equals(setter))
					{
						m = m2;
						break;
					}
				}
				Assert.notNull(m, getFieldName() + " Expecting a public setter");
				m.setAccessible(true);
				setter(m);
			}
			
		}
	}
}
