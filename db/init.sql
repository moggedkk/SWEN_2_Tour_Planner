--
-- PostgreSQL database schema for Tour Planner
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- 1. Create Users Table (Initial)
CREATE TABLE public.users (
    id SERIAL PRIMARY KEY,
    username text NOT NULL UNIQUE,
    hash text NOT NULL
);

-- 2. Create Sessions Table
CREATE TABLE public.sessions (
    id SERIAL PRIMARY KEY,
    user_id integer NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    session_value text NOT NULL
);

-- 3. Add sessionId to Users
ALTER TABLE public.users ADD COLUMN session_id integer REFERENCES public.sessions(id);

-- 4. Create Tours Table (Initial)
CREATE TABLE public.tours (
    id SERIAL PRIMARY KEY,
    tour_name text NOT NULL,
    start_location text,
    end_location text,
    description text,
    distance double precision,
    estimated_time integer,
    user_id integer NOT NULL REFERENCES public.users(id) ON DELETE CASCADE
);

-- 5. Create Difficulties Table
CREATE TABLE public.difficulties (
    id SERIAL PRIMARY KEY,
    tour_id integer REFERENCES public.tours(id) ON DELETE CASCADE,
    difficulty_value text NOT NULL
);

-- 6. Create Transport Types Table
CREATE TABLE public.transport_types (
    id SERIAL PRIMARY KEY,
    tour_id integer REFERENCES public.tours(id) ON DELETE CASCADE,
    transport_type_value text NOT NULL
);

-- 7. Add difficulty_id and transport_type_id to Tours
ALTER TABLE public.tours ADD COLUMN difficulty_id integer REFERENCES public.difficulties(id);
ALTER TABLE public.tours ADD COLUMN transport_type_id integer REFERENCES public.transport_types(id);

-- 8. Create Tour Logs Table
CREATE TABLE public.tour_logs (
    id SERIAL PRIMARY KEY,
    date_time timestamp without time zone NOT NULL,
    comment text,
    total_distance double precision,
    total_time double precision,
    rating integer,
    file_path text,
    tour_id integer NOT NULL REFERENCES public.tours(id) ON DELETE CASCADE,
    difficulty_id integer REFERENCES public.difficulties(id)
);

ALTER TABLE public.users OWNER TO postgres;
ALTER TABLE public.sessions OWNER TO postgres;
ALTER TABLE public.tours OWNER TO postgres;
ALTER TABLE public.difficulties OWNER TO postgres;
ALTER TABLE public.transport_types OWNER TO postgres;
ALTER TABLE public.tour_logs OWNER TO postgres;
