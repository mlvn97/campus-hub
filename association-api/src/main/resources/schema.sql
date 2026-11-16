CREATE UNIQUE INDEX IF NOT EXISTS uk_application_pending_user_association
    ON membership_applications (association_id, user_id)
    WHERE status = 'PENDING';
