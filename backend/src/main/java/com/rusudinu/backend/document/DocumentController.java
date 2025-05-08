package com.rusudinu.backend.document;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {
	private final DocumentService documentService;

	@PostMapping
//    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST')")
	public Document uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam Long documentId) {
		return documentService.uploadDocument(file, documentId);
	}

	@PostMapping("/create-document")
//    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST')")
	public Document createDocument(@RequestParam(required = true) String documentName) {
		return documentService.createDocument(documentName);
	}

	@GetMapping("/need-review")
	public List<Document> getNeedReviewDocuments() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return documentService.getNeedReviewDocuments(authentication.getName());
	}

	@GetMapping("{documentId}")
	public Document getDocumentById(@PathVariable Long documentId) {
		return documentService.getDocumentById(documentId);
	}

	@GetMapping("/download/{storedDocumentName}")
	public ResponseEntity<byte[]> getDocument(@PathVariable String storedDocumentName) {
		byte[] documentContent = documentService.getDocument(storedDocumentName);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.inline().filename(storedDocumentName).build());
		headers.set("X-Frame-Options", "SAMEORIGIN");

		return new ResponseEntity<>(documentContent, headers, HttpStatus.OK);
	}

	@PostMapping("/economic-and-social/{documentId}")
	public Document addEconomicAndSocialCouncilComment(@PathVariable Long documentId, @RequestParam String comment) {
		return documentService.addEconomicAndSocialCouncilComment(documentId, comment);
	}

	@PostMapping("/economic-and-social-document/{documentId}")
	public Document addEconomicAndSocialCouncilDocument(@RequestParam("file") MultipartFile file, @PathVariable Long documentId) {
		return documentService.addEconomicAndSocialCouncilDocument(file, documentId);
	}

	@PostMapping("/general-secretariat/{documentId}")
	public Document addGeneralSecretariatComment(@PathVariable Long documentId, @RequestParam String comment) {
		return documentService.addGeneralSecretariatComment(documentId, comment);
	}

	@PostMapping("/general-secretariat-document/{documentId}")
	public Document addGeneralSecretariatDocument(@RequestParam("file") MultipartFile file, @PathVariable Long documentId) {
		return documentService.addGeneralSecretariatDocument(file, documentId);
	}

	@PostMapping("/legislative-council/{documentId}")
	public Document addLegislativeCouncilComment(@PathVariable Long documentId, @RequestParam String comment) {
		return documentService.addLegislativeCouncilComment(documentId, comment);
	}

	@PostMapping("/legislative-council-document/{documentId}")
	public Document addLegislativeCouncilDocument(@RequestParam("file") MultipartFile file, @PathVariable Long documentId) {
		return documentService.addLegislativeCouncilDocument(file, documentId);
	}

	@PostMapping("/transport-committee/{documentId}")
	public Document addTransportCommitteeComment(@PathVariable Long documentId, @RequestParam String comment) {
		return documentService.addTransportCommitteeComment(documentId, comment);
	}

	@PostMapping("/transport-committee-document/{documentId}")
	public Document addTransportCommitteeDocument(@RequestParam("file") MultipartFile file, @PathVariable Long documentId) {
		return documentService.addTransportCommitteeDocument(file, documentId);
	}

	@PostMapping("/politics-committee/{documentId}")
	public Document addPoliticsCommitteeComment(@PathVariable Long documentId, @RequestParam String comment) {
		return documentService.addPoliticsCommitteeComment(documentId, comment);
	}

	@PostMapping("/politics-committee-document/{documentId}")
	public Document addPoliticsCommitteeDocument(@RequestParam("file") MultipartFile file, @PathVariable Long documentId) {
		return documentService.addPoliticsCommitteeDocument(file, documentId);
	}

	@PostMapping("/specialty-commission")
	public Document addSpecialtyCommissionDocument(@RequestParam("file") MultipartFile file, @RequestParam Long documentId) {
		return documentService.addSpecialtyCommissionDocument(file, documentId);
	}

	@GetMapping("/document-valid/{documentId}")
	public boolean getDocumentValid(@PathVariable Long documentId) {
		return documentService.documentHasAllCommentsValid(documentId);
	}

	@GetMapping("/document-doc-valid/{documentId}")
	public boolean getDocumentDocValid(@PathVariable Long documentId) {
		return documentService.validateDocumentHash(documentId);
	}

	@GetMapping("/specialty-commission-doc-valid/{documentId}")
	public boolean getSpecialtyCommissionDocValid(@PathVariable Long documentId) {
		return documentService.validateSpecialtyCommissionDocumentHash(documentId);
	}

	@PostMapping("/start-plenary-session/{documentId}")
	public Document startPlenarySession(@PathVariable Long documentId) {
		return documentService.startPlenarySession(documentId);
	}

	@PostMapping("/vote/{documentId}")
	public Document vote(@PathVariable Long documentId, @RequestParam String vote) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return documentService.vote(documentId, vote, authentication.getName());
	}

	@GetMapping("/status/{documentId}")
	public DocumentStatus getDocumentStatus(@PathVariable Long documentId) {
		return documentService.getDocumentStatus(documentId);
	}
}
