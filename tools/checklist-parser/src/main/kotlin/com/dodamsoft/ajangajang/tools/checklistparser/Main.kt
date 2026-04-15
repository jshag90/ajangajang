package com.dodamsoft.ajangajang.tools.checklistparser

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import kotlin.system.exitProcess

@Serializable
enum class DevelopmentAreaType { SOCIAL, LANGUAGE, COGNITIVE, PHYSICAL }

@Serializable
data class ChecklistItem(val id: String, val text: String)

@Serializable
data class DevelopmentArea(val type: DevelopmentAreaType, val items: List<ChecklistItem>)

@Serializable
data class GrowthTip(val title: String? = null, val body: String)

@Serializable
data class ChecklistStage(
    val months: Int,
    val displayLabel: String,
    val areas: List<DevelopmentArea>,
    val doctorQuestions: List<String> = emptyList(),
    val growthTips: List<GrowthTip> = emptyList()
)

@Serializable
data class ChecklistCatalog(val version: Int, val stages: List<ChecklistStage>)

private val stageRegex = Regex("""(\d+)months\.html""")

private val areaMappings = linkedMapOf(
    "collapseSocial" to DevelopmentAreaType.SOCIAL,
    "collapseLanguage" to DevelopmentAreaType.LANGUAGE,
    "collapseCognitive" to DevelopmentAreaType.COGNITIVE,
    "collapsePhysical" to DevelopmentAreaType.PHYSICAL,
)

fun main(args: Array<String>) {
    if (args.size < 2) {
        System.err.println("Usage: checklist-parser <input-dir> <output-json-path>")
        System.err.println("Example: checklist-parser 'C:/Users/wltjs/OneDrive/바탕 화면/little-steps/checklist' app/src/main/assets/checklist.json")
        exitProcess(1)
    }

    val inputDir = File(args[0])
    val outputFile = File(args[1])
    require(inputDir.isDirectory) { "Input dir not found: ${inputDir.absolutePath}" }

    val htmlFiles = inputDir
        .listFiles { _, name -> stageRegex.matches(name) }
        ?.sortedBy { stageRegex.find(it.name)!!.groupValues[1].toInt() }
        ?: emptyList()

    require(htmlFiles.isNotEmpty()) {
        "No {N}months.html files found in ${inputDir.absolutePath}"
    }

    println("Found ${htmlFiles.size} HTML files in ${inputDir.absolutePath}")

    val stages = htmlFiles.map { file ->
        val months = stageRegex.find(file.name)!!.groupValues[1].toInt()
        val doc = Jsoup.parse(file, Charsets.UTF_8.name())
        parseStage(months, doc)
    }

    val catalog = ChecklistCatalog(version = 1, stages = stages)
    val json = Json {
        prettyPrint = true
        explicitNulls = false
        encodeDefaults = true
    }
    val jsonText = json.encodeToString(ChecklistCatalog.serializer(), catalog)

    outputFile.parentFile?.mkdirs()
    outputFile.writeText(jsonText, Charsets.UTF_8)

    val totalItems = stages.sumOf { stage -> stage.areas.sumOf { it.items.size } }
    println("Wrote ${outputFile.absolutePath}")
    println("Summary: ${stages.size} stages, $totalItems total items")
    stages.forEach { stage ->
        val itemCount = stage.areas.sumOf { it.items.size }
        println(
            "  ${stage.displayLabel.padEnd(6)}  items=$itemCount  " +
                "doctorQs=${stage.doctorQuestions.size}  tips=${stage.growthTips.size}"
        )
    }
}

private fun parseStage(months: Int, doc: Document): ChecklistStage {
    val stageKey = "${months}m"
    val areas = areaMappings.mapNotNull { (collapseId, type) ->
        parseArea(collapseId, type, doc, stageKey)
    }
    return ChecklistStage(
        months = months,
        displayLabel = if (months < 36) "${months}개월" else "${months / 12}세",
        areas = areas,
        doctorQuestions = parseDoctorQuestions(doc),
        growthTips = parseGrowthTips(doc),
    )
}

private fun parseArea(
    collapseId: String,
    type: DevelopmentAreaType,
    doc: Document,
    stageKey: String,
): DevelopmentArea? {
    val collapse = doc.getElementById(collapseId) ?: return null
    val items = collapse.select("div.form-check").mapNotNull { formCheck ->
        val input = formCheck.selectFirst("input[type=checkbox]") ?: return@mapNotNull null
        val rawId = input.attr("id").trim().ifBlank { return@mapNotNull null }
        val label = formCheck.selectFirst("label[for=$rawId]")
            ?: formCheck.selectFirst("label")
        val text = label?.text()?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        ChecklistItem(id = "$stageKey.$rawId", text = text)
    }
    return DevelopmentArea(type = type, items = items)
}

private fun parseDoctorQuestions(doc: Document): List<String> {
    val alert = doc.select("div.alert.alert-info").firstOrNull { element ->
        element.selectFirst("h5")?.text()?.contains("의사") == true
    } ?: return emptyList()
    return alert.select("ul > li").map { it.text().trim() }.filter { it.isNotBlank() }
}

private fun parseGrowthTips(doc: Document): List<GrowthTip> {
    // Style A (2~15 months): cards with .card > .card-body > (optional h6) + p
    val cardTips = doc.select("div.card div.card-body").mapNotNull { body ->
        val title = body.selectFirst("h6")?.text()?.trim()?.takeIf { it.isNotBlank() }
        val bodyText = body.selectFirst("p")?.text()?.trim()?.takeIf { it.isNotBlank() }
            ?: return@mapNotNull null
        GrowthTip(title = title, body = bodyText)
    }
    if (cardTips.isNotEmpty()) return cardTips

    // Style B (18+ months): <h3>...배우고 성장...</h3> followed by <ul><li>...</li></ul>
    val tipsHeading = doc.select("h3").firstOrNull { h3 ->
        val text = h3.text()
        text.contains("배우고 성장") || text.contains("발달 촉진") || text.contains("학습")
    } ?: return emptyList()

    var sibling = tipsHeading.nextElementSibling()
    while (sibling != null && sibling.tagName() != "ul") {
        sibling = sibling.nextElementSibling()
    }
    val ul = sibling ?: return emptyList()
    return ul.select("> li").mapNotNull { li ->
        val text = li.text().trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
        GrowthTip(title = null, body = text)
    }
}
