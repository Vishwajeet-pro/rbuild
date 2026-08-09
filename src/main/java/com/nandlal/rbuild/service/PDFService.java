package com.nandlal.rbuild.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PDFService {

	public byte[] createResume(
			String fullName,
			String email,
			String phone,
			String summary,
			String skills,
			String experience,
			String education,
			String template) {

		String html = buildHtml(fullName, email, phone, summary, skills, experience, education, template);

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withHtmlContent(html, null);
			builder.toStream(outputStream);
			builder.run();
			return outputStream.toByteArray();
		}
		catch (Exception exception) {
			throw new IllegalStateException("Unable to create resume PDF", exception);
		}
	}

	private String buildHtml(
			String fullName,
			String email,
			String phone,
			String summary,
			String skills,
			String experience,
			String education,
			String template) {

		TemplateStyle style = TemplateStyle.from(template);

		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8" />
				    <style>
				        @page { size: A4; margin: 0.6in; }
				        body {
				            margin: 0;
				            font-family: Arial, Helvetica, sans-serif;
				            color: #111827;
				            font-size: 11pt;
				            line-height: 1.45;
				        }
				        h1 {
				            margin: 0;
				            font-size: %s;
				            letter-spacing: 0;
				            color: %s;
				            text-align: %s;
				        }
				        .contact {
				            margin: 6px 0 18px;
				            color: #374151;
				            text-align: %s;
				            font-size: 10pt;
				        }
				        .section {
				            margin-top: 15px;
				        }
				        h2 {
				            margin: 0 0 6px;
				            padding-bottom: 4px;
				            border-bottom: %s;
				            color: %s;
				            font-size: 11pt;
				            letter-spacing: 0.5px;
				            text-transform: uppercase;
				        }
				        .content {
				            margin: 0;
				        }
				        p {
				            margin: 0 0 7px;
				        }
				        ul, ol {
				            margin: 0 0 7px 20px;
				            padding: 0;
				        }
				        li {
				            margin: 0 0 3px;
				        }
				        b, strong {
				            font-weight: 700;
				        }
				        i, em {
				            font-style: italic;
				        }
				    </style>
				</head>
				<body>
				    <h1>%s</h1>
				    <div class="contact">%s | %s</div>

				    <div class="section">
				        <h2>Professional Summary</h2>
				        <div class="content">%s</div>
				    </div>
				    <div class="section">
				        <h2>Skills</h2>
				        <div class="content">%s</div>
				    </div>
				    <div class="section">
				        <h2>Experience</h2>
				        <div class="content">%s</div>
				    </div>
				    <div class="section">
				        <h2>Education</h2>
				        <div class="content">%s</div>
				    </div>
				</body>
				</html>
				""".formatted(
				style.nameSize(),
				style.accentColor(),
				style.headerAlign(),
				style.headerAlign(),
				style.sectionBorder(),
				style.accentColor(),
				escapeText(fullName),
				escapeText(email),
				escapeText(phone),
				cleanRichText(summary),
				cleanRichText(skills),
				cleanRichText(experience),
				cleanRichText(education));
	}

	private String escapeText(String value) {
		if (value == null) {
			return "";
		}

		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	private String cleanRichText(String value) {
		if (value == null || value.isBlank()) {
			return "<p></p>";
		}

		String cleaned = value
				.replaceAll("(?is)<script.*?>.*?</script>", "")
				.replaceAll("(?is)<style.*?>.*?</style>", "")
				.replaceAll("(?i)\\s+on\\w+\\s*=\\s*\"[^\"]*\"", "")
				.replaceAll("(?i)\\s+on\\w+\\s*=\\s*'[^']*'", "")
				.replaceAll("(?i)\\s+on\\w+\\s*=\\s*[^\\s>]+", "")
				.replaceAll("(?i)<\\s*(?!/?(?:p|br|b|strong|i|em|ul|ol|li|div)\\b)[^>]*>", "")
				.replaceAll("(?i)<br\\b[^>]*>", "<br />")
				.replaceAll("(?i)<(ul|ol|li|b|strong|i|em)\\b[^>]*>", "<$1>");

		cleaned = keepParagraphAlignment(cleaned)
				.replaceAll("(?i)</div\\s*>", "</p>")
				.replaceAll("(?i)</(p|ul|ol|li|b|strong|i|em)\\s*>", "</$1>");

		if (!cleaned.matches("(?is).*<(p|ul|ol|div|li|br)\\b.*")) {
			cleaned = "<p>" + escapeText(cleaned) + "</p>";
		}

		return cleaned;
	}

	private String keepParagraphAlignment(String value) {
		return value.replaceAll("(?i)<(p|div)\\b(?=[^>]*text-align\\s*:\\s*left)[^>]*>", "<p style=\"text-align: left;\">")
				.replaceAll("(?i)<(p|div)\\b(?=[^>]*text-align\\s*:\\s*center)[^>]*>", "<p style=\"text-align: center;\">")
				.replaceAll("(?i)<(p|div)\\b(?=[^>]*text-align\\s*:\\s*right)[^>]*>", "<p style=\"text-align: right;\">")
				.replaceAll("(?i)<(p|div)\\b(?=[^>]*align\\s*=\\s*[\"']?left)[^>]*>", "<p style=\"text-align: left;\">")
				.replaceAll("(?i)<(p|div)\\b(?=[^>]*align\\s*=\\s*[\"']?center)[^>]*>", "<p style=\"text-align: center;\">")
				.replaceAll("(?i)<(p|div)\\b(?=[^>]*align\\s*=\\s*[\"']?right)[^>]*>", "<p style=\"text-align: right;\">")
				.replaceAll("(?i)<(p|div)\\b[^>]*>", "<p>");
	}

	private record TemplateStyle(String nameSize, String accentColor, String headerAlign, String sectionBorder) {
		private static TemplateStyle from(String template) {
			return switch (template == null ? "" : template) {
				case "compact" -> new TemplateStyle("22pt", "#111827", "left", "1px solid #9ca3af");
				case "modern" -> new TemplateStyle("24pt", "#1f4e79", "left", "2px solid #1f4e79");
				default -> new TemplateStyle("25pt", "#111827", "center", "1px solid #111827");
			};
		}
	}
}
