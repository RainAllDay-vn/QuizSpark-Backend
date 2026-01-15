# Plan: Stream Progress Updates for PDF Extraction

## Goal
Enhance the PDF extraction process to stream real-time progress updates (current page, total pages) to the client.

## Steps

### 1. Update DTO
**File:** `src/main/java/com/quizspark/quizsparkserver/dto/ai/AiResponseDTO.java`
- Add `private Integer current;`
- Add `private Integer total;`
- These fields will hold the progress information.

### 2. Refactor AiFileProcessService
**File:** `src/main/java/com/quizspark/quizsparkserver/services/AiFileProcessService.java`

#### A. Update `extractContentFromPdf`
- Pass the `FluxSink<AiResponseDTO>` down to helper methods `classifyPages` and `extractContent`.

#### B. Update `classifyPages`
- Signature: `private List<ProcessedPageDTO> classifyPages(DbFile file, FluxSink<AiResponseDTO> sink)`
- Action:
    - Emit an initial status: `sink.next(AiResponseDTO.builder().status("analyze").message("Analyzing document structure...").build());`
    - (Optional) Emit status if falling back to vision classification.

#### C. Update `extractContent`
- Signature: `public List<ProcessedPageDTO> extractContent(DbFile file, List<ProcessedPageDTO> pages, FluxSink<AiResponseDTO> sink)`
- Action:
    - Inside the `for` loop (iterating through pages), emit progress updates:
      ```java
      sink.next(AiResponseDTO.builder()
          .status("progress")
          .current(i + 1)
          .total(pages.size())
          .message("Processing page " + (i + 1) + " of " + pages.size())
          .build());
      ```

#### D. Fix Recursion/Overloading
- `extractContent` currently has two overloads.
    - `extractContent(String fileId)` (public API) -> calls `extractContentFromPdf` -> calls `classifyPages` -> calls `extractContent(DbFile file, List<ProcessedPageDTO> pages)`.
- Ensure the public `extractContent(String fileId)` handles the Flux correctly (it already returns Flux, so it's fine).
- The internal `extractContent` logic needs to accept the Sink.

## Execution Order
1. Apply changes to `AiResponseDTO`.
2. Apply changes to `AiFileProcessService`.
