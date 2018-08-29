package me.elon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class XmlMain
{
	public static boolean containJmx(String location)
	{
		boolean has = false;
		File f = new File(location);
		File[] files = f.listFiles();
		for (File file : files) {
			if (file.getName().toLowerCase().endsWith(".jmx"))
			{
				has = true;
				break;
			}
		}
		return has;
	}

	public static ArrayList<String> getfolders(ArrayList<String> folders, String location)
	{
		File f = new File(location);
		if (f.isDirectory())
		{
			if (containJmx(location)) {
				folders.add(location);
			}
			File[] files = f.listFiles();
			for (File file : files) {
				if (file.isDirectory())
				{
					String path = file.getAbsolutePath();
					getfolders(folders, path);
				}
			}
		}
		return folders;
	}

	public static String testPlanBuffer(ArrayList<String> plansDirs)
	{
		StringBuffer buffer = new StringBuffer();
		for (String dir : plansDirs)
		{
			String testplan = String.format("<testplans dir=\"%s\" includes=\"*.jmx\" />", new Object[] { dir });
			buffer.append("\t\t\t" + testplan + "\n");
		}
		System.out.println(buffer.toString());
		return buffer.toString();
	}

	public static void writeBuildXml(String path, String data)
	{
		try
		{
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fileWritter = new FileWriter(path, false);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(data);
			bufferWritter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String xmlContent(String buildxmlTemplate, ArrayList<String> folders)
	{
		StringBuffer content = new StringBuffer();
		File file = new File(buildxmlTemplate);
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				if (tempString.contains("testplans"))
				{
					String plansString = testPlanBuffer(folders);
					content.append(plansString);
				}
				else
				{
					content.append(tempString + "\n");
				}
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			if (reader != null) {
				try
				{
					reader.close();
				}
				catch (IOException localIOException1) {}
			}
		}
		finally
		{
			if (reader != null) {
				try
				{
					reader.close();
				}
				catch (IOException localIOException2) {}
			}
		}
		return content.toString();
	}

	public static void main(String[] args)
	{
		String buildxmlTemplate = args[0];
		String testCaseFolder = args[1];
		String buildXml = args[2];

		ArrayList<String> folders = new ArrayList();

		getfolders(folders, testCaseFolder);
		String data = xmlContent(buildxmlTemplate, folders);
		writeBuildXml(buildXml, data);
	}
}
