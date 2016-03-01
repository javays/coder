package com.ivy.coder.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

public class FileUtils {

	private static final Logger logger = Logger.getLogger(FileUtils.class);
	
	/**
     * 判断源文件是否已存在
     * @param packName
     * @param fileName
     * @return
     */
    public boolean isSrcExist(String packName, String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return false;
        }
        
        String src = SysProps.getProject_src();
        if (!StringUtils.isEmpty(packName)) {
            src += packName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
        }
        File file = new File(src, fileName);
        return file.exists();
    }
	
	/**
	 * 写入内容到文件
	 * @param filePath
	 * @param content
	 */
	public static void write(String filePath, String content) {
		if (StringUtils.isEmpty(filePath) || StringUtils.isEmpty(content)) {
			logger.warn("input param is empty");
			return;
		}
		
		File file = new File(filePath);
		if (file.exists() && file.isDirectory()) {
			logger.warn("exist a same name dir!!!");
			throw new RuntimeException("exist a same name dir!!!");
		}
		
		if(!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		try (FileWriter fw = new FileWriter(file);) {
			fw.write(content);
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		}
	}
	
    
	/**
	 * 追加内容到文件
	 * @param filePath
	 * @param content
	 */
	public static void appendToEnd(String filePath, String content) {
		if (StringUtils.isEmpty(filePath) || StringUtils.isEmpty(content)) {
			logger.warn("input param is empty");
			return;
		}
		
		File file = new File(filePath);
		if (file.exists() && file.isDirectory()) {
			logger.warn("exist a same name dir!!!");
			throw new RuntimeException("exist a same name dir!!!");
		}
		
		if (!file.exists()) {
			write(filePath, content);
			return;
		} 
		
		File tmp = new File(file.getAbsolutePath() + ".tmp");
		try (
				BufferedReader br = new BufferedReader(new FileReader(file));
				PrintWriter pw = new PrintWriter(tmp);
				
			) {
			boolean writed = false;
			boolean canAppend = false;
			int leftBraceCount = 0;
			String line = null;
			while ((line = br.readLine()) != null) {
				if ((leftBraceCount == 1 && !writed && canAppend)
						|| (line.trim().equals("}") && leftBraceCount == 1 && !writed)) {
					pw.println();
					pw.println(content);
					writed = true;
				}
				
				pw.println(line);
				
				List<String> result = StringUtils.find(line, "[\\{\\}]");
				for (String str : result) {
					if ("{".equals(str)) {
						leftBraceCount ++;
						if (leftBraceCount > 1) {
							canAppend = true;
						}
					} else if ("}".equals(str)) {
						leftBraceCount --;
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
		}
		
		file.delete();
		tmp.renameTo(file);
	}
	
	
	
	public static void main(String[] args) throws IOException {
		// 字符串连接，concat = "ABCD"
		String concat = Stream.of("A", "B", "C", "D").reduce("1", String::concat); 
		System.out.println(concat);
		// 求最小值，minValue = -3.0
		double minValue = Stream.of(-1.5, 1.0, -3.0, -2.0).reduce(-4.5, Double::min); 
		System.out.println(minValue);
		// 求和，sumValue = 10, 有起始值
		int sumValue = Stream.of(1, 2, 3, 4).reduce(11, Integer::sum);
		System.out.println(sumValue);
		// 求和，sumValue = 10, 无起始值
		sumValue = Stream.of(1, 2, 3, 4).reduce(Integer::sum).get();
		System.out.println(sumValue);
		// 过滤，字符串连接，concat = "ace"
		concat = Stream.of("a", "B", "c", "D", "e", "F").
		 filter(x -> x.compareTo("Z") > 0).
		 reduce("", String::concat);
		System.out.println(concat);
	}
}
