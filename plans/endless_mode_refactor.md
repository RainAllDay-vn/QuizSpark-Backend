# Endless Mode Refactor Plan

## Goal
Switch Endless Mode from "auto-fetching on answer" to "manual on-demand fetching" and allow removing questions from a practice session.

## Architecture

### 1. DTOs
*   **`PracticeFetchRequestDTO.java`** (New)
    *   `int numberOfQuestions` (default 5?)
    *   `int aiQuestions` (optional, default 0. Can be explicitly requested to force AI generation).

### 2. API Endpoints
*   **POST** `/api/v1/practice/{practiceId}/fetch`
    *   Body: `PracticeFetchRequestDTO`
    *   Response: `List<PracticeQuestionDTO>` (The newly added questions)
*   **DELETE** `/api/v1/practice/{practiceId}/questions/{index}`
    *   Note: Identifying by `index` since `PracticeQuestion` is identified by `{practiceId, index}`.
    *   Response: `void` (200 OK)

### 3. Service Logic (`PracticeService`)

#### Modification: `answerPracticeQuestion`
*   **Remove**: The call to `fetchNextQuestions(practiceId)`. Answering should only record the answer.

#### New Method: `fetchQuestionsManual(String practiceId, int numberOfQuestions, int aiQuestions)`
1.  Load Practice. Validate ownership.
2.  Determine connection to `QuestionBank`.
3.  **Strategy**:
    *   Calculate `questionsNeededFromDB = numberOfQuestions - aiQuestions`.
    *   If `questionsNeededFromDB > 0`:
        *   Fetch unseen questions from DB (excluding IDs already in `PracticeQuestion` list).
        *   Limit to `questionsNeededFromDB`.
    *   Calculate `shortfall = questionsNeededFromDB - foundDbQuestions.size()`.
    *   `totalAiNeeded = aiQuestions + shortfall`.
    *   If `totalAiNeeded > 0`:
        *   Call `AiService.generateAdaptiveQuestions(practice, totalAiNeeded)`.
        *   Save generated questions to DB.
        *   Add to list of questions to add.
    *   **Insertion Logic**:
        *   Determine `nextIndex`.
        *   **Important**: Do NOT use `currentQuestions.size()` because deletion might cause gaps.
        *   Use `currentQuestions.stream().mapToInt(id -> id.getIndex()).max().orElse(-1) + 1`.
        *   Create `PracticeQuestion` entries.
        *   Save and return DTOs.

#### New Method: `discardQuestion(String practiceId, int index)`
1.  Find `PracticeQuestion` by `{practiceId, index}`.
2.  Validate ownership.
3.  Delete the entity.
    *   *Note*: The `Practice` entity might need `orphanRemoval=true` or explicit repository delete.
    *   Since `PracticeQuestion` is the owner of the relation (mapped by `practice` in parent?), we can delete via repository.

### 4. AI Service Logic (`AiService`)

#### Modification: `generateAdaptiveQuestions`
*   Update signature to accept `int count`.
*   Pass `count` to the prompt to instruct the AI how many questions to generate.

## Step-by-Step Implementation

1.  **Create DTO**: `PracticeFetchRequestDTO`.
2.  **Update AiService**: Support requesting `N` questions.
3.  **Update PracticeService**:
    *   Refactor `fetchNextQuestions` into `fetchQuestionsManual` with new logic.
    *   Fix index calculation bug (use max+1 instead of size).
    *   Remove auto-fetch trigger.
    *   Implement `discardQuestion`.
4.  **Update PracticeController**: Add endpoints.
5.  **Verify**: Run tests or verify via architecture check.
