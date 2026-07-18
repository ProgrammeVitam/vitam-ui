dbIam.tokens.createIndex(
    { updatedDate: 1 },
    { name: "idx_token_date", expireAfterSeconds: 0, background: true }
);
