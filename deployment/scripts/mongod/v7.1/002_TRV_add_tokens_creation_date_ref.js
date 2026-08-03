dbIam.tokens.updateMany(
    { "updatedDate": { $exists: true } },
    [
        {
            $set: {
                "createdDate": "$updatedDate"
            }
        }
    ]
);
