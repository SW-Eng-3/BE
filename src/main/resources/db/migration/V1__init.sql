CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    restricted_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    major VARCHAR(100),
    current_company VARCHAR(255),
    job_category VARCHAR(100),
    country VARCHAR(100),
    bio TEXT,
    points INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP
);

CREATE TABLE posts (
    id UUID PRIMARY KEY,
    author_id UUID REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_anonymous BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE comments (
    id UUID PRIMARY KEY,
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id),
    content TEXT NOT NULL,
    is_recommended BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE mentoring_requests (
    id UUID PRIMARY KEY,
    mentee_id UUID REFERENCES users(id),
    mentor_id UUID REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    message TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE mentor_schedules (
    id UUID PRIMARY KEY,
    mentor_id UUID REFERENCES users(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL
);

CREATE TABLE reports (
    id UUID PRIMARY KEY,
    reporter_id UUID NOT NULL REFERENCES users(id),
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    reason VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE point_histories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    amount INTEGER NOT NULL,
    reason VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
