package com.skillbuilder.agent.db

import androidx.room.*

@Entity(tableName = "skill_cards")
data class SkillCardEntity(
    @PrimaryKey val id: String,
    val skillName: String,
    val status: String,
    val overallScore: Float,
    val knowledgeCoverage: Float,
    val practicalPerformance: Float,
    val conceptualUnderstanding: Float,
    val sourceConfidence: Float,
    val masteredConceptsJson: String,
    val weakAreasJson: String,
    val prerequisitesJson: String,
    val referenceSourcesJson: String,
    val externalSourcesJson: String,
    val lastVerifiedTimestamp: Long
)

@Dao
interface SkillCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillCard(card: SkillCardEntity)

    @Query("SELECT * FROM skill_cards WHERE id = :id")
    suspend fun getSkillCardById(id: String): SkillCardEntity?

    @Query("SELECT * FROM skill_cards ORDER BY lastVerifiedTimestamp DESC")
    suspend fun getAllSkillCards(): List<SkillCardEntity>
}

@Database(entities = [SkillCardEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun skillCardDao(): SkillCardDao
}
