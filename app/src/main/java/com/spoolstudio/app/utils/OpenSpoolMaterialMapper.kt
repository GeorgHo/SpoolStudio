package com.spoolstudio.app.utils

object OpenSpoolMaterialMapper {

    private val plaMaterials = setOf(
        "PLA",
        "PLA+",
        "PLA PLUS",
        "APLA",
        "HT-PLA"
    )

    private val absMaterials = setOf(
        "ABS",
        "ABS+",
        "ABS PLUS",
        "MABS"
    )

    private val asaMaterials = setOf(
        "ASA",
        "ASA+",
        "ASA PLUS"
    )

    private val paMaterials = setOf(
        "PA",
        "PA (NYLON)",
        "NYLON",
        "PA6",
        "PA12",
        "PA612",
        "PAHT",
        "COPA"
    )

    private val petgMaterials = setOf(
        "PETG",
        "PETG+",
        "PETG PLUS"
    )

    private val tpuMaterials = setOf(
        "TPU",
        "TPE",
        "TPR"
    )

    fun toOpenSpoolType(
        material: String,
        variant: String?
    ): String? {
        val normalizedMaterial = material.trim().uppercase()
        val normalizedVariant = variant
            ?.trim()
            ?.uppercase()
            .orEmpty()

        val variantTokens = normalizedVariant
            .replace("-", " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .toSet()

        val hasCf = "CF" in variantTokens
        val hasGf = "GF" in variantTokens

        return when {
            normalizedMaterial in setOf(
                "PET-CF",
                "PPA-CF",
                "PPA-GF"
            ) -> normalizedMaterial

            normalizedMaterial == "PA6" && hasCf ->
                "PA6-CF"

            normalizedMaterial in plaMaterials && hasCf ->
                "PLA-CF"

            normalizedMaterial in petgMaterials && hasCf ->
                "PETG-CF"

            normalizedMaterial == "PET" && hasCf ->
                "PET-CF"

            normalizedMaterial in asaMaterials && hasCf ->
                "ASA-CF"

            normalizedMaterial in absMaterials && hasGf ->
                "ABS-GF"

            normalizedMaterial in paMaterials && hasCf ->
                "PA-CF"

            normalizedMaterial in paMaterials && hasGf ->
                "PA-GF"

            normalizedMaterial == "PPA" && hasCf ->
                "PPA-CF"

            normalizedMaterial == "PPA" && hasGf ->
                "PPA-GF"

            normalizedMaterial == "PPS" && hasCf ->
                "PPS-CF"

            normalizedMaterial == "PE" && hasCf ->
                "PE-CF"

            normalizedMaterial == "PP" && hasCf ->
                "PP-CF"

            normalizedMaterial == "PP" && hasGf ->
                "PP-GF"

            normalizedMaterial in plaMaterials &&
                    normalizedVariant in setOf("AERO", "AIR", "LW", "FOAMING") ->
                "PLA-AERO"

            normalizedMaterial in asaMaterials &&
                    normalizedVariant in setOf("AERO", "AIR", "LW", "FOAMING") ->
                "ASA-Aero"

            normalizedMaterial in tpuMaterials &&
                    normalizedVariant in setOf("AMS", "AMS COMPATIBLE") ->
                "TPU-AMS"

            normalizedMaterial in plaMaterials ->
                "PLA"

            normalizedMaterial in absMaterials ->
                "ABS"

            normalizedMaterial in asaMaterials ->
                "ASA"

            normalizedMaterial in petgMaterials ->
                "PETG"

            normalizedMaterial in paMaterials ->
                "PA"

            normalizedMaterial in tpuMaterials ->
                "TPU"

            normalizedMaterial in setOf(
                "BVOH",
                "HIPS",
                "PC",
                "PCTG",
                "PE",
                "PP",
                "PPS",
                "PVA",
                "PHA",
                "EVA"
            ) ->
                normalizedMaterial

            else ->
                null
        }
    }
}