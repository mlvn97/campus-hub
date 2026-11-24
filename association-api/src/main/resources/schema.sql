CREATE UNIQUE INDEX IF NOT EXISTS uk_subscription_pending_user_association
    ON subscriptions (association_id, user_id)
    WHERE status = 'PENDING';
