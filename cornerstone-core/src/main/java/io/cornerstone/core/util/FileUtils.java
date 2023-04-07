package io.cornerstone.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.experimental.UtilityClass;

import org.springframework.util.StringUtils;

@UtilityClass
public class FileUtils {

	public static String normalizePath(String input) {
		if (!StringUtils.hasLength(input)) {
			return input;
		}
		List<String> list = new ArrayList<>();
		for (String s : input.split("/|\\\\")) {
			if (s.isEmpty()) {
				continue;
			}
			switch (s) {
				case "." -> {
				}
				case ".." -> {
					if ((list.size() > 0) && !list.get(list.size() - 1).equals("..")) {
						list.remove(list.size() - 1);
					}
					else {
						list.add("..");
					}
				}
				default -> list.add(s);
			}
		}
		String path = String.join("/", list);
		if (input.charAt(0) == '/') {
			path = '/' + path;
		}
		if (path.startsWith("../")) {
			path = path.substring(2);
		}
		while (path.startsWith("/../")) {
			path = path.substring(3);
		}
		if (path.equals("/..")) {
			path = "/";
		}
		if ((input.charAt(input.length() - 1) == '/') && (path.charAt(path.length() - 1) != '/')) {
			path = path + '/';
		}
		return path;
	}

	public static File zip(File file) throws Exception {
		return zip(file, null);
	}

	public static File zip(File file, File zipFile) throws Exception {
		if (zipFile == null) {
			zipFile = defaultZipFileName(file);
		}
		if (!file.exists() || !file.canRead()) {
			throw new RuntimeException(file + "doesn't exist or cannot read");
		}
		if (file.isDirectory()) {
			try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
				zipDirctory(out, file, "");
			}
		}
		else {
			try (FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);
					FileOutputStream fos = new FileOutputStream(zipFile);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					ZipOutputStream zos = new ZipOutputStream(bos);) {
				byte[] buf = new byte[1024];
				int len;
				ZipEntry ze = new ZipEntry(file.getName());
				zos.putNextEntry(ze);
				while ((len = bis.read(buf)) != -1) {
					zos.write(buf, 0, len);
					zos.flush();
				}
			}
		}
		return zipFile;
	}

	private static File defaultZipFileName(File file) {
		String zipFile;
		if (file.isDirectory()) {
			zipFile = file.getAbsolutePath();
			if (zipFile.endsWith("/") || zipFile.endsWith("\\")) {
				zipFile = zipFile.substring(0, zipFile.length() - 1);
			}
			zipFile += ".zip";
		}
		else {
			zipFile = file + ".zip";
		}
		return new File(zipFile);
	}

	private static void zipDirctory(ZipOutputStream out, File file, String base) throws Exception {
		if (file.isDirectory()) {
			File[] fl = file.listFiles();
			if (fl == null) {
				return;
			}
			if (!base.isEmpty()) {
				out.putNextEntry(new ZipEntry(base + "/"));
			}
			base = base.length() == 0 ? "" : base + "/";
			for (File element : fl) {
				zipDirctory(out, element, base + element.getName());
			}
		}
		else {
			out.putNextEntry(new ZipEntry(base));
			FileInputStream in = new FileInputStream(file);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			in.close();
		}
	}

	public static Map<String, String> parseManifestFile(File jarfile) {
		try (JarFile jar = new JarFile(jarfile)) {
			Manifest mf = jar.getManifest();
			if (mf == null) {
				return null;
			}
			Attributes attrs = mf.getMainAttributes();
			if (attrs == null) {
				return null;
			}
			Map<String, String> map = new HashMap<>();
			mf.getMainAttributes().forEach((k, v) -> {
				map.put(k.toString(), v.toString());
			});
			return map;
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
