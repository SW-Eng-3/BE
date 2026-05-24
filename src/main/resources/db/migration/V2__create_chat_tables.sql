CREATE TABLE chat_rooms (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES users(id),
    senior_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_message TEXT,
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_chat_rooms_student_senior UNIQUE (student_id, senior_id)
);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    message_type VARCHAR(50) NOT NULL DEFAULT 'CHAT',
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_chat_rooms_student_id ON chat_rooms(student_id);
CREATE INDEX idx_chat_rooms_senior_id ON chat_rooms(senior_id);
CREATE INDEX idx_chat_messages_room_created_at ON chat_messages(room_id, created_at);
