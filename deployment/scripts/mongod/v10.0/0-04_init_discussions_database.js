if (!dbDiscussions.getCollectionNames().includes('discussions')) {
    dbDiscussions.createCollection('discussions');
}

if (!dbDiscussions.getCollectionNames().includes('discussions_read')) {
    dbDiscussions.createCollection('discussions_read');
}

dbDiscussions.discussions_read.createIndex(
    { userId: 1, discussionId: 1 },
    { name: 'idx_discussions_read_user_discussion', unique: true }
);
