package com.skillbuilder.core.learning

import com.skillbuilder.core.domain.contracts.SkillRepository
import com.skillbuilder.core.domain.model.SkillCard

class InMemorySkillRepository : SkillRepository {
    private val skillCards = mutableMapOf<String, SkillCard>()

    override fun saveSkillCard(skillCard: SkillCard) {
        skillCards[skillCard.id] = skillCard
    }

    override fun getSkillCard(skillId: String): SkillCard? {
        return skillCards[skillId]
    }

    override fun getAllSkillCards(): List<SkillCard> {
        return skillCards.values.toList()
    }
}
