CREATE TABLE IF NOT EXISTS public.comment (
	id BIGSERIAL PRIMARY KEY,
	event_id BIGINT NOT NULL,
	author_id BIGINT,
	content TEXT NOT NULL,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.comment_pre_moderation (
	event_id BIGSERIAL NOT NULL,
	forbidden_word varchar(32) NOT NULL,
	CONSTRAINT comment_pre_moderation_pkey
		PRIMARY KEY (event_id, forbidden_word)
);