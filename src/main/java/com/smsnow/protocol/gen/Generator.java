package com.smsnow.protocol.gen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.smsnow.protocol.Attribute;
import com.smsnow.protocol.Format;
import com.smsnow.protocol.Protocol;

public class Generator {
	
	private static String generateSourceCode(JMeta meta)
	{
		Assert.notNull(meta.className, "Class name not specified");
		final JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setName(meta.className);
		
		if(StringUtils.hasText(meta.packageName))
			javaClass.setPackage(meta.packageName);

		javaClass.addAnnotation(Protocol.class).setStringValue("name", meta.getName()+"");
				
		javaClass.addInterface(Serializable.class);
		javaClass.addField()
		  .setName("serialVersionUID")
		  .setType("long")
		  .setLiteralInitializer("1L")
		  .setPrivate()
		  .setStatic(true)
		  .setFinal(true);

		for(Entry<JField, JFormat> e : meta.fields.entrySet())
		{
			JField fld = e.getKey();
			JFormat fmt = e.getValue();
			Attribute attrib;
			try {
				attrib = Attribute.valueOf((fmt.attribute+"").toUpperCase());
			} catch (Exception e1) {
				attrib = Attribute.UNDEF;
			}
			
			javaClass.addProperty(fld.type, fld.name)
			.getField()
			.setPrivate()
			.addAnnotation(Format.class)
			.setEnumValue("attribute", attrib)
			.setLiteralValue("offset", fmt.offset+"")
			.setLiteralValue("length", fmt.length+"")
			.setStringValue("constant", fmt.constant+"");
		}
		

		javaClass.addMethod()
		  .setConstructor(true)
		  .setPublic()
		  .setBody("");
		
		return javaClass.toString();
	}
	/**
	 * Generate a java source file at the specified directory.
	 * @param meta
	 * @param dir
	 * @return the absolute path name for the generated file.
	 * @throws IOException
	 */
	public static String run(JMeta meta, String dir, String templFile) throws IOException
	{
		File f = new File(dir);
		if(!f.exists())
			f.mkdirs();
		if(!f.isDirectory())
			throw new IOException("Not a valid directory - "+dir);
		
		String java = generateSourceCode(meta);
		Path p = f.toPath();
		p = p.resolve(meta.className+".java");
		
		try(BufferedWriter bw = Files.newBufferedWriter(p, 
				StandardCharsets.UTF_8, StandardOpenOption.CREATE, 
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))
		{
			bw.write("/* Generated by SMSNOW protocol CodeGen on "+new Date()+". \n\n");
			StringBuilder s = new StringBuilder();
			for( String l : Files.readAllLines(Paths.get(templFile)))
			{
				s.append(l).append("\n");
			}
			s.append("*/");
			bw.write(s.toString());
			bw.write("\n");
			bw.write(java);
			bw.flush();
		}
		
		return p.toAbsolutePath().toString();
	}
	
	private static void printUsage()
	{
		String s = new StringBuilder("Usage: ")
		.append("java ")
		.append(Generator.class.getName())
		.append("\n -t <template_file_path> *\n")
		.append(" -c <generated_class_name> *\n")
		.append(" -d <target_dir_path> \n")
		.append(" -p <generated_package_name>\n")
		.append(" -a <protocol_annot_name>\n")
		.toString();
		
		System.err.println(s);
	}
	/**
	 * Generate a java source file with the given control parameters.
	 * @param args
	 */
	public static void run(List<String> args)
	{
		int i;
		String template = "", className = "", pkgName="", destn = "", name = "";
		short typeCode = 0;
		if((i = args.indexOf("-t")) != -1)
		{
			template = args.get(i+1);
		}
		else
		{
			printUsage();
			System.exit(0);
		}
		if((i = args.indexOf("-c")) != -1)
		{
			className = args.get(i+1);
		}
		else
		{
			printUsage();
			System.exit(0);
		}
		
		if((i = args.indexOf("-p")) != -1)
		{
			pkgName = args.get(i+1);
		}
		if((i = args.indexOf("-a")) != -1)
		{
			name = args.get(i+1);
		}
		else
		{
			name = className.toUpperCase();
		}
		if((i = args.indexOf("-d")) != -1)
		{
			destn = args.get(i+1);
		}
		else
		{
			destn = System.getProperty("java.io.tmpdir");
		}
		
		List<Throwable> err = new ArrayList<>();
		System.out.println("Start job..");
		try
		{
			JMeta meta = FormatParser.parse(template, err);
			if(err.isEmpty())
			{
				meta.setClassName(className);
				meta.setPackageName(pkgName);
				meta.setName(name);
				meta.setTypeCode(typeCode);
				String fileGen = run(meta, destn, template);
				System.out.println("File generated: "+fileGen);
			}
			else
			{
				for(Throwable t : err)
					t.printStackTrace();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("End job..");
	}
	public static void main(String[] args) throws IOException {
		run(Arrays.asList(args));
	}
	

}
