create table if not exists hit (
    id serial primary key,
    app varchar(255) not null,
    uri varchar(255) not null,
    ip varchar(255) not null,
    created_at timestamp not null
);