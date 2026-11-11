package com.example.data.ai

import com.example.data.model.ImageSizeOption
import kotlinx.coroutines.delay

data class GeneratedDiagramResult(
    val prompt: String,
    val model: String = "gemini-3-pro-image-preview",
    val imageSize: ImageSizeOption,
    val title: String,
    val description: String,
    val diagramType: String,
    val components: List<String>,
    val keyTakeaways: List<String>,
    val offlineNotice: String = "Live high-resolution generation with gemini-3-pro-image-preview requires Firebase AI Logic setup. Showing offline study concept preview at selected resolution."
)

interface AiRepository {
    suspend fun generateStudyDiagram(
        prompt: String,
        size: ImageSizeOption,
        subjectName: String = "Computer Science"
    ): Result<GeneratedDiagramResult>
}

class DefaultAiRepository : AiRepository {

    override suspend fun generateStudyDiagram(
        prompt: String,
        size: ImageSizeOption,
        subjectName: String
    ): Result<GeneratedDiagramResult> {
        // Simulate synthesis time
        delay(1200)

        val cleanPrompt = prompt.trim()
        val title = when {
            cleanPrompt.contains("bcnf", ignoreCase = true) || cleanPrompt.contains("normalization", ignoreCase = true) ->
                "Relational Database Normalization Hierarchy (1NF -> 2NF -> 3NF -> BCNF)"
            cleanPrompt.contains("os", ignoreCase = true) || cleanPrompt.contains("process", ignoreCase = true) ->
                "Operating System Process State Lifecycle & Scheduling Flow"
            cleanPrompt.contains("network", ignoreCase = true) || cleanPrompt.contains("tcp", ignoreCase = true) ->
                "TCP 3-Way Handshake & Sliding Window Flow Diagram"
            cleanPrompt.contains("tree", ignoreCase = true) || cleanPrompt.contains("b+", ignoreCase = true) ->
                "B+ Tree Multi-Level Indexing Node Structure"
            else ->
                "Academic Concept Map: ${cleanPrompt.ifBlank { "Subject Core Principles" }}"
        }

        val components = when {
            cleanPrompt.contains("normalization", ignoreCase = true) -> listOf(
                "1NF: Eliminate repeating groups & atomic attribute values",
                "2NF: Remove partial dependencies on candidate keys",
                "3NF: Eliminate transitive dependencies on non-prime attributes",
                "BCNF: Strict determinant rule — every X in X->Y must be a superkey"
            )
            cleanPrompt.contains("os", ignoreCase = true) -> listOf(
                "New State: Process initialized in PCB",
                "Ready State: Queued in scheduler run-queue",
                "Running State: Dispatcher assigns CPU core",
                "Waiting / Blocked: I/O burst or semaphore wait",
                "Terminated: PCB deallocated and exit code returned"
            )
            else -> listOf(
                "Input Node: Fundamental Axioms & Definitions",
                "Core Transform Layer: Mathematical or Algorithmic Relationships",
                "Branching Nodes: Sub-theorems, Variants and Protocols",
                "Output / Synthesis: Applications, Best Practices and Edge Cases"
            )
        }

        val takeaways = listOf(
            "Optimal for exam quick revision and visual memory retention",
            "Resolution format configured: ${size.label} (${size.resolution})",
            "Synthesized using architecture targeting: gemini-3-pro-image-preview"
        )

        return Result.success(
            GeneratedDiagramResult(
                prompt = cleanPrompt,
                model = "gemini-3-pro-image-preview",
                imageSize = size,
                title = title,
                description = "High-fidelity academic illustration designed for $subjectName examination preparation.",
                diagramType = "Structured Educational Concept Graph",
                components = components,
                keyTakeaways = takeaways
            )
        )
    }
}
