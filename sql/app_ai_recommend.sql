-- AI conversational movie recommendation tables
-- Execute on the current runtime database: movie_recommend_system_cqupt

CREATE TABLE IF NOT EXISTS app_ai_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'Legacy front user id from yonghu.id',
    title VARCHAR(200) NOT NULL DEFAULT '新对话' COMMENT 'Conversation title',
    status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT 'active/archived',
    is_cold_start TINYINT NOT NULL DEFAULT 0 COMMENT 'Whether this conversation is for cold-start preference collection',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_ai_conversation_user_deleted_updated (user_id, is_deleted, updated_at),
    INDEX idx_ai_conversation_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI recommendation conversation';

CREATE TABLE IF NOT EXISTS app_ai_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL COMMENT 'user/assistant/system',
    content MEDIUMTEXT NOT NULL,
    movie_id BIGINT DEFAULT NULL COMMENT 'Primary recommended app_movie.id when applicable',
    recommendation_reason TEXT DEFAULT NULL COMMENT 'Recommendation explanation',
    tokens_used INT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_message_conversation_created (conversation_id, created_at),
    INDEX idx_ai_message_movie (movie_id),
    CONSTRAINT fk_ai_message_conversation FOREIGN KEY (conversation_id) REFERENCES app_ai_conversation(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI recommendation conversation message';

CREATE TABLE IF NOT EXISTS app_user_preference (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'Legacy front user id from yonghu.id',
    favorite_genres VARCHAR(500) DEFAULT NULL COMMENT 'JSON array or comma text',
    favorite_years VARCHAR(200) DEFAULT NULL COMMENT 'JSON array or selected year range',
    favorite_regions VARCHAR(500) DEFAULT NULL COMMENT 'JSON array or comma text',
    mood_preferences VARCHAR(500) DEFAULT NULL COMMENT 'JSON array or comma text',
    watch_frequency VARCHAR(100) DEFAULT NULL COMMENT 'Watch frequency code/text',
    disliked_genres VARCHAR(500) DEFAULT NULL COMMENT 'JSON array or comma text',
    cold_start_completed TINYINT NOT NULL DEFAULT 0,
    cold_start_completed_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_preference_user (user_id),
    INDEX idx_user_preference_completed (cold_start_completed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI recommendation user preference';

CREATE TABLE IF NOT EXISTS app_recommendation_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'Legacy front user id from yonghu.id',
    conversation_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL COMMENT 'Assistant recommendation message id',
    movie_id BIGINT NOT NULL COMMENT 'app_movie.id',
    feedback_type VARCHAR(32) NOT NULL COMMENT 'like/dislike/watched/want_watch/not_interested',
    feedback_reason TEXT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recommend_feedback_user_movie (user_id, movie_id),
    INDEX idx_recommend_feedback_conversation (conversation_id),
    INDEX idx_recommend_feedback_message (message_id),
    UNIQUE KEY uk_recommend_feedback_user_message_movie (user_id, message_id, movie_id),
    CONSTRAINT fk_recommend_feedback_conversation FOREIGN KEY (conversation_id) REFERENCES app_ai_conversation(id),
    CONSTRAINT fk_recommend_feedback_message FOREIGN KEY (message_id) REFERENCES app_ai_message(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI recommendation feedback';
