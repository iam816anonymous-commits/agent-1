package com.skillbuilder.agent.db

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skillbuilder.core.domain.contracts.SkillRepository
import com.skillbuilder.core.domain.model.SkillCard
import com.skillbuilder.core.domain.model.SkillScoreBreakdown
import com.skillbuilder.core.domain.model.SkillStatus
import com.skillbuilder.core.domain.model.SourceProvenance
import kotlinx.coroutines.runBlocking

class RoomSkillRepository(
    private val dao: SkillCardDao,
    private val gson: Gson = Gson()
) : SkillRepository {

    override fun saveSkillCard(skillCard: SkillCard) {
        val entity = SkillCardEntity(
            id = skillCard.id,
            skillName = skillCard.skillName,
            status = skillCard.status.name,
            overallScore = skillCard.scoreBreakdown.overallScore,
            knowledgeCoverage = skillCard.scoreBreakdown.knowledgeCoverage,
            practicalPerformance = skillCard.scoreBreakdown.practicalPerformance,
            conceptualUnderstanding = skillCard.scoreBreakdown.conceptualUnderstanding,
            sourceConfidence = skillCard.scoreBreakdown.sourceConfidence,
            masteredConceptsJson = gson.toJson(skillCard.masteredConcepts),
            weakAreasJson = gson.toJson(skillCard.weakAreas),
            prerequisitesJson = gson.toJson(skillCard.prerequisites),
            referenceSourcesJson = gson.toJson(skillCard.referenceSources),
            externalSourcesJson = gson.toJson(skillCard.externalSources),
            lastVerifiedTimestamp = skillCard.lastVerifiedTimestamp
        )
        runBlocking {
            dao.insertSkillCard(entity)
        }
    }

    override fun getSkillCard(skillId: String): SkillCard? {
        val entity = runBlocking { dao.getSkillCardById(skillId) } ?: return null
        return mapEntityToDomain(entity)
    }

    override fun getAllSkillCards(): List<SkillCard> {
        val entities = runBlocking { dao.getAllSkillCards() }
        return entities.map { mapEntityToDomain(it) }
    }

    private fun mapEntityToDomain(entity: SkillCardEntity): SkillCard {
        val stringListType = object : TypeToken<List<String>>() {}.type
        val provenanceListType = object : TypeToken<List<SourceProvenance>>() {}.type

        return SkillCard(
            id = entity.id,
            skillName = entity.skillName,
            status = try { SkillStatus.valueOf(entity.status) } catch (e: Exception) { SkillStatus.UNKNOWN },
            scoreBreakdown = SkillScoreBreakdown(
                knowledgeCoverage = entity.knowledgeCoverage,
                practicalPerformance = entity.practicalPerformance,
                conceptualUnderstanding = entity.conceptualUnderstanding,
                sourceConfidence = entity.sourceConfidence,
                overallScore = entity.overallScore
            ),
            masteredConcepts = gson.fromJson(entity.masteredConceptsJson, stringListType) ?: emptyList(),
            weakAreas = gson.fromJson(entity.weakAreasJson, stringListType) ?: emptyList(),
            prerequisites = gson.fromJson(entity.prerequisitesJson, stringListType) ?: emptyList(),
            referenceSources = gson.fromJson(entity.referenceSourcesJson, provenanceListType) ?: emptyList(),
            externalSources = gson.fromJson(entity.externalSourcesJson, provenanceListType) ?: emptyList(),
            lastVerifiedTimestamp = entity.lastVerifiedTimestamp
        )
    }
}
