CREATE TABLE account(
    id bigserial PRIMARY KEY,
    currency text NOT NULL,
    created_at timestamp NOT NULL
)

CREATE TABLE activity(
    id bigserial PRIMARY KEY,
    created_at timestamp NOT NULL,
    owner_account_id bigint NOT NULL,
    source_account_id bigint NOT NULL,
    target_account_id bigint NOT NULL,
    amount bigint NOT NULL
)
