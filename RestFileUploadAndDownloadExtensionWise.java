package com.example.demo.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@org.springframework.web.bind.annotation.RestController
public class RestFileUploadAndDownloadExtensionWise {

	@Value("${app.file-base-path}")
	String fileBasePath;

	@RequestMapping(path = "/test")
	private void test() {
		System.out.println("I'm up :)");
	}

	@GetMapping(path = "/downloadFile/{filename}", produces = MediaType.ALL_VALUE)
	private ResponseEntity downloadFile(@PathVariable("filename") String fileName) {
		String fileNameDown = StringUtils.cleanPath(fileName);
		String extension = getExtension(fileNameDown);
		String basepath = fileBasePath + "\\" + extension + "\\";
		File file = new File(basepath + fileNameDown);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
		Path path = Paths.get(file.getAbsolutePath());
		ByteArrayResource resource;
		ResponseEntity response;
		try {
			resource = new ByteArrayResource(Files.readAllBytes(path));
			response = ResponseEntity.ok().headers(httpHeaders).contentLength(resource.contentLength()).body(resource);
		} catch (IOException e) {
			e.printStackTrace();
			response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(fileName + " -- Not Found!");
		}
		return response;
	}

	@PostMapping(path = "/uploadFile", produces = MediaType.APPLICATION_JSON_VALUE)
	private ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile mf) throws IOException {
		String filename = StringUtils.cleanPath(mf.getOriginalFilename());
		String extension = getExtension(filename);
		String basepath = fileBasePath + "\\" + extension;
		File fbp = new File(basepath);
		if (!fbp.exists()) {
			fbp.mkdir();
		}
		Path p = Paths.get(fbp + "\\" + filename);
		Files.copy(mf.getInputStream(), p, StandardCopyOption.REPLACE_EXISTING);
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
				.path(filename).toUriString();
		Map<String, String> response = new HashMap<String, String>();
		response.put("downloadUrl", fileDownloadUri);
		response.put("FID", String.valueOf(System.currentTimeMillis()));
		return ResponseEntity.ok(response);
	}

	static String getExtension(String filename) {
		String ext;
		if (filename.lastIndexOf(".") > 0)
			ext = filename.substring(filename.lastIndexOf(".") + 1);
		else
			ext = "misc";
		return ext;
	}

}
