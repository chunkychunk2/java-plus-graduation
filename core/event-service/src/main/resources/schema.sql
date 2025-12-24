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