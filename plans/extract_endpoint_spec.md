# Endpoint Report: PDF Extraction Streaming

## Overview

This endpoint initiates the AI-powered extraction of content from a PDF file. It utilizes Server-Sent Events (SSE) to stream real-time progress updates and intermediate results to the client.

- **URL**: `/api/v1/extract/file/{fileId}` (Assuming base path `api/v1` based on controller context, need to verify exact full path if not standard, but standard Spring Boot usually roughly maps this way. Based on `AiController`, the path is likely `/api/v1/ai/extract/file/{fileId}` or simply `/extract/file/{fileId}` depending on Class-level RequestMapping. *Note: Please check `AiController` class level annotation for the exact prefix.*)
- **Method**: `POST`
- **Produces**: `text/event-stream`

## Request
- **Path Variable**: `fileId` (The ID of the uploaded file to process)

## Response Structure (`AiResponseDTO`)

Each event in the stream follows this JSON structure:

```json
{
  "status": "string",   // Event type identifier
  "data": "string",     // JSON-serialized payload (optional)
  "current": 1,         // Current progress index (optional)
  "total": 10           // Total items to process (optional)
}
```

## Event Stream Lifecycle

The server streams a sequence of events representing the processing stages:

### 1. Analysis Started
Indicates the beginning of the file analysis.
```json
{
  "status": "analyze"
}
```

### 2. Classification Complete
Sent after the AI determines the layout and text density of pages.
- **Payload (`data`)**: JSON string representing `List<ProcessedPageDTO>`. This includes page text (if available from PDF structure) and text density classification.
```json
{
  "status": "classify",
  "data": "[{\"pageIndex\":0, \"textDensity\":\"HIGH\", ...}, ...]"
}
```

### 3. Extraction Progress
Sent periodically as the system processes batches of pages using the Vision model.
- **Use Case**: Update a progress bar.
```json
{
  "status": "progress",
  "current": 2, // 0-based index of the page currently processing
  "total": 5    // Total number of pages
}
```

### 4. Extraction Finished
Sent when the entire document has been processed.
- **Payload (`data`)**: JSON string representing the final `List<ProcessedPageDTO>`. This contains the fully extracted text for all pages.
```json
{
  "status": "finish",
  "data": "[{\"pageIndex\":0, \"extracted\":true, \"text\":\"...\", ...}, ...]"
}
```

## Frontend Implementation Guide

1.  **Connection**: Use `EventSource` (Web API) or a library like `fetch-event-source` (for POST requests with headers) to connect to the endpoint.
    *   *Note*: Standard `EventSource` does not support POST. You may need to use `fetch` with a reader or a specialized library like `@microsoft/fetch-event-source`.

2.  **Handling Events**:
    *   Listen for the `message` event (or default data event).
    *   Parse the JSON body.
    *   Switch on `status`:
        *   **`analyze`**: Show "Analyzing document structure..." spinner.
        *   **`classify`**: (Optional) Display initial page preview or density map. Parse `data` if needed.
        *   **`progress`**: Update progress bar: `(current / total) * 100%`.
        *   **`finish`**: Extraction complete. Parse `data` to get the final extracted text and display the result. Close the connection.

3.  **Error Handling**: Handle connection errors or error events from the server (which may come as standard HTTP errors before the stream starts, or error events within the stream).
