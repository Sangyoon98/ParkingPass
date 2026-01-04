-- Phase 2: 회원 시스템 및 주차장 공유 기능

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    name TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS provider TEXT DEFAULT 'local',
    ADD COLUMN IF NOT EXISTS provider_user_id TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_provider_user
    ON users(provider, provider_user_id)
    WHERE provider_user_id IS NOT NULL;

-- 주차장 테이블 확장
ALTER TABLE parking_lot
    ADD COLUMN IF NOT EXISTS owner_id UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS join_code TEXT UNIQUE;

-- 주차장 멤버십 테이블
CREATE TABLE IF NOT EXISTS parking_lot_member (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lot(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'MEMBER',
    status TEXT NOT NULL DEFAULT 'PENDING',
    invited_by UUID REFERENCES users(id),
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(parking_lot_id, user_id)
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_parking_lot_owner ON parking_lot(owner_id);
CREATE INDEX IF NOT EXISTS idx_parking_lot_public ON parking_lot(is_public);
CREATE INDEX IF NOT EXISTS idx_parking_lot_member_user ON parking_lot_member(user_id);
CREATE INDEX IF NOT EXISTS idx_parking_lot_member_status ON parking_lot_member(status);
