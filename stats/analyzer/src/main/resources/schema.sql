CREATE TABLE IF NOT EXISTS event_similarities (
    first_event BIGINT NOT NULL,
    second_event BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    PRIMARY KEY(first_event, second_event)
);

CREATE TABLE IF NOT EXISTS user_actions (
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    user_score DOUBLE PRECISION NOT NULL,
    timestamp_action TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY(user_id, event_id)
);