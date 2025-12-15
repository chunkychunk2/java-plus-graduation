CREATE TABLE IF NOT EXISTS public.users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL,
    CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS public.category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.event (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    annotation VARCHAR(2000) NOT NULL,
    description VARCHAR(7000),
    state VARCHAR(32) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_on TIMESTAMP,
    category_id BIGINT NOT NULL,
    initiator_id BIGINT NOT NULL,
    paid BOOLEAN DEFAULT FALSE,
    request_moderation BOOLEAN DEFAULT TRUE,
    participant_limit INT DEFAULT 0,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    CONSTRAINT event_category_fk
        FOREIGN KEY (category_id)
        REFERENCES public.category(id)
        ON DELETE RESTRICT,  -- Запрет удаления используемой категории
    CONSTRAINT event_users_fk
        FOREIGN KEY (initiator_id)
        REFERENCES public.users(id)
        ON DELETE CASCADE,  -- Удаление событий при удалении организатора
    CONSTRAINT event_state_check
        CHECK (state IN ('PENDING', 'PUBLISHED', 'CANCELED'))
);

CREATE TABLE IF NOT EXISTS public.compilation (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    pinned BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS public.event_compilation (
    event_id BIGINT NOT NULL,
    compilation_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, compilation_id),
    CONSTRAINT fk_event
        FOREIGN KEY (event_id)
        REFERENCES public.event(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_compilation
        FOREIGN KEY (compilation_id)
        REFERENCES public.compilation(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.participation_request (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT participation_request_users_fk
        FOREIGN KEY (requester_id)
        REFERENCES public.users(id)
        ON DELETE CASCADE,  -- Удаление запросов при удалении пользователя
    CONSTRAINT participation_request_event_fk
        FOREIGN KEY (event_id)
        REFERENCES public.event(id)
        ON DELETE CASCADE,  -- Удаление запросов при удалении события
    CONSTRAINT participation_status_check
        CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELED')),
    CONSTRAINT unique_request UNIQUE (requester_id, event_id)
);


CREATE TABLE IF NOT EXISTS public.comment (
	id BIGSERIAL PRIMARY KEY,
	event_id BIGINT NOT NULL,
	author_id BIGINT,
	content TEXT NOT NULL,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT comment_event_fk FOREIGN KEY (event_id)
		REFERENCES public."event"(id)
		ON DELETE CASCADE,
	CONSTRAINT comment_users_fk
		FOREIGN KEY (author_id)
		REFERENCES public.users(id)
		ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS public.comment_pre_moderation (
	event_id BIGSERIAL NOT NULL,
	forbidden_word varchar(32) NOT NULL,
	CONSTRAINT comment_pre_moderation_pkey
		PRIMARY KEY (event_id, forbidden_word),
	CONSTRAINT comment_pre_moderation_event_fk
		FOREIGN KEY (event_id)
		REFERENCES public.event (id)
		ON DELETE CASCADE
);