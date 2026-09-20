CREATE TABLE notifications (
    id UUID NOT NULL,
    recipient_user_id UUID NOT NULL,
    notification_type VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    related_incident_id UUID,
    related_work_order_id UUID,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMPTZ,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_incident FOREIGN KEY (related_incident_id) REFERENCES incidents (id),
    CONSTRAINT fk_notifications_work_order FOREIGN KEY (related_work_order_id) REFERENCES work_orders (id)
);

CREATE INDEX idx_notifications_recipient_created ON notifications (recipient_user_id, created_at);
CREATE INDEX idx_notifications_recipient_unread ON notifications (recipient_user_id, read);
