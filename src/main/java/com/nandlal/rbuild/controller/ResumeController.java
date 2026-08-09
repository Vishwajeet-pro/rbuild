package com.nandlal.rbuild.controller;

import com.nandlal.rbuild.service.PDFService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResumeController {

	private final PDFService pdfService;

	public ResumeController(PDFService pdfService) {
		this.pdfService = pdfService;
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@PostMapping("/download")
	public ResponseEntity<byte[]> downloadResume(
			@RequestParam String fullName,
			@RequestParam String email,
			@RequestParam String phone,
			@RequestParam String summary,
			@RequestParam String skills,
			@RequestParam String experience,
			@RequestParam String education,
			@RequestParam(defaultValue = "classic") String template) {

		byte[] resume = pdfService.createResume(fullName, email, phone, summary, skills, experience, education, template);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
				.contentType(MediaType.APPLICATION_PDF)
				.body(resume);
	}
}
